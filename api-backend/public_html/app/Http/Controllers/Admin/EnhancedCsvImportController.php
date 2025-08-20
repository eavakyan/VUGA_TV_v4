<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\Content;
use App\Models\V2\Category;
use App\Models\V2\AppLanguage;
use App\Models\V2\ContentAgeLimit;
use App\Models\V2\AgeLimit;
use App\Models\V2\ContentSource;
use App\Models\V2\ContentTrailer;
use App\Models\V2\Actor;
use App\Models\V2\ContentCast;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use League\Csv\Reader;
use League\Csv\Statement;
use Exception;
use Illuminate\Support\Str;

class EnhancedCsvImportController extends Controller
{
    /**
     * Display the enhanced CSV import form
     */
    public function index()
    {
        return view('admin.enhanced-csv-import.index');
    }

    /**
     * Process the enhanced CSV file upload and import content with cast
     */
    public function import(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'csv_file' => 'required|file|mimes:csv,txt|max:20480', // 20MB max
            'dry_run' => 'boolean',
            'import_cast' => 'boolean',
            'import_sources' => 'boolean'
        ]);

        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }

        $isDryRun = $request->get('dry_run', false);
        $importCast = $request->get('import_cast', true);
        $importSources = $request->get('import_sources', true);
        
        $results = [
            'success' => 0,
            'errors' => [],
            'warnings' => [],
            'imported_content' => [],
            'imported_actors' => [],
            'imported_sources' => []
        ];

        try {
            $csv = Reader::createFromPath($request->file('csv_file')->getPathname(), 'r');
            $csv->setHeaderOffset(0);
            
            $headers = $csv->getHeader();
            $requiredHeaders = [
                'title', 'description', 'type', 'release_year',
                'language', 'genres', 'is_featured', 'vertical_poster', 'horizontal_poster'
            ];
            
            // Validate headers
            $missingHeaders = array_diff($requiredHeaders, $headers);
            if (!empty($missingHeaders)) {
                throw new Exception('Missing required headers: ' . implode(', ', $missingHeaders));
            }

            $records = Statement::create()->process($csv);
            
            DB::beginTransaction();
            
            foreach ($records as $offset => $row) {
                $lineNumber = $offset + 2; // +1 for header, +1 for human-readable
                
                try {
                    $this->validateRow($row, $lineNumber);
                    
                    if (!$isDryRun) {
                        $content = $this->importRow($row, $importCast, $importSources);
                        $results['imported_content'][] = $content->title;
                        
                        // Import cast if provided
                        if ($importCast && !empty($row['cast'])) {
                            $castResult = $this->importCast($content, $row['cast'], $row);
                            $results['imported_actors'] = array_merge(
                                $results['imported_actors'], 
                                $castResult['actors']
                            );
                        }
                        
                        // Import video sources if provided
                        if ($importSources && !empty($row['video_url'])) {
                            $sourceResult = $this->importVideoSource($content, $row);
                            if ($sourceResult) {
                                $results['imported_sources'][] = $content->title;
                            }
                        }
                    }
                    
                    $results['success']++;
                    
                } catch (Exception $e) {
                    $results['errors'][] = "Line {$lineNumber}: " . $e->getMessage();
                    
                    if (count($results['errors']) > 50) {
                        throw new Exception('Too many errors. Import aborted.');
                    }
                }
            }
            
            if ($isDryRun || !empty($results['errors'])) {
                DB::rollBack();
                
                if ($isDryRun) {
                    $results['warnings'][] = 'Dry run completed. No data was imported.';
                }
            } else {
                DB::commit();
            }
            
        } catch (Exception $e) {
            DB::rollBack();
            
            return redirect()->back()
                ->with('error', 'Import failed: ' . $e->getMessage())
                ->with('import_results', $results);
        }

        return redirect()->back()
            ->with('success', "Import completed. {$results['success']} records processed.")
            ->with('import_results', $results);
    }

    /**
     * Validate a single CSV row with enhanced fields
     */
    private function validateRow($row, $lineNumber)
    {
        $rules = [
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'type' => 'required|in:movie,series',
            'duration' => 'nullable|integer|min:0',
            'release_year' => 'required|integer|between:1900,' . (date('Y') + 1),
            'ratings' => 'nullable|numeric|between:0,10',
            'language' => 'required|string',
            'is_featured' => 'required|in:0,1,yes,no,true,false',
            'vertical_poster' => 'nullable|string|max:500',
            'horizontal_poster' => 'nullable|string|max:500',
            'trailer_url' => 'nullable|url|max:500',
            'video_url' => 'nullable|string|max:500',
            'video_quality' => 'nullable|string|max:50',
            'cast' => 'nullable|string|max:2000',
            'director' => 'nullable|string|max:255',
            'producer' => 'nullable|string|max:255',
            'writer' => 'nullable|string|max:255',
            'music_composer' => 'nullable|string|max:255',
            'cinematographer' => 'nullable|string|max:255',
            'editor' => 'nullable|string|max:255',
            'production_company' => 'nullable|string|max:255',
            'budget' => 'nullable|string|max:50',
            'box_office' => 'nullable|string|max:50',
            'imdb_id' => 'nullable|string|max:20',
            'tmdb_id' => 'nullable|string|max:20'
        ];

        $validator = Validator::make($row, $rules);
        
        if ($validator->fails()) {
            throw new Exception($validator->errors()->first());
        }
    }

    /**
     * Import a single row of content with enhanced data
     */
    private function importRow($row, $importCast = true, $importSources = true)
    {
        // Convert type
        $type = $row['type'] === 'movie' ? 1 : 2;
        
        // Convert boolean fields
        $isFeatured = in_array(strtolower($row['is_featured']), ['1', 'yes', 'true']) ? 1 : 0;
        
        // Find or create language
        $language = AppLanguage::firstOrCreate(
            ['title' => trim($row['language'])],
            ['code' => strtolower(substr(trim($row['language']), 0, 2))]
        );
        
        // Create content with enhanced metadata
        $content = Content::create([
            'title' => trim($row['title']),
            'description' => trim($row['description']),
            'type' => $type,
            'duration' => !empty($row['duration']) ? (int)$row['duration'] : null,
            'release_year' => (int)$row['release_year'],
            'ratings' => !empty($row['ratings']) ? (float)$row['ratings'] : 0,
            'language_id' => $language->app_language_id,
            'vertical_poster' => !empty($row['vertical_poster']) ? trim($row['vertical_poster']) : null,
            'horizontal_poster' => !empty($row['horizontal_poster']) ? trim($row['horizontal_poster']) : null,
            'genre_ids' => '', // Will be updated below
            'is_featured' => $isFeatured,
            'is_show' => 1,
            'total_view' => !empty($row['initial_views']) ? (int)$row['initial_views'] : 0,
            'total_download' => 0,
            'total_share' => 0
        ]);
        
        // Store additional metadata in a JSON column if needed
        if (!empty($row['director']) || !empty($row['producer']) || !empty($row['production_company'])) {
            // Store in content_metadata table or as JSON
            $metadata = [
                'director' => $row['director'] ?? null,
                'producer' => $row['producer'] ?? null,
                'writer' => $row['writer'] ?? null,
                'music_composer' => $row['music_composer'] ?? null,
                'cinematographer' => $row['cinematographer'] ?? null,
                'editor' => $row['editor'] ?? null,
                'production_company' => $row['production_company'] ?? null,
                'budget' => $row['budget'] ?? null,
                'box_office' => $row['box_office'] ?? null,
                'imdb_id' => $row['imdb_id'] ?? null,
                'tmdb_id' => $row['tmdb_id'] ?? null
            ];
            // You can store this in a separate metadata table or as JSON
        }
        
        // Process genres
        if (!empty($row['genres'])) {
            $genreNames = array_map('trim', explode(',', $row['genres']));
            $genreIds = [];
            
            foreach ($genreNames as $genreName) {
                if (empty($genreName)) continue;
                
                $genre = Category::firstOrCreate(['title' => $genreName]);
                $genreIds[] = $genre->category_id;
                
                // Create content_category relationship
                DB::table('content_category')->insertOrIgnore([
                    'content_id' => $content->content_id,
                    'category_id' => $genre->category_id
                ]);
            }
            
            // Update genre_ids field (for backward compatibility)
            $content->update(['genre_ids' => implode(',', $genreIds)]);
        }
        
        // Process trailers
        if (!empty($row['trailer_url'])) {
            $this->importTrailer($content, $row['trailer_url']);
        }
        
        // Process age limits if provided
        if (!empty($row['age_rating'])) {
            $this->importAgeRating($content, $row['age_rating']);
        }
        
        return $content;
    }

    /**
     * Import cast and crew for a content
     */
    private function importCast($content, $castString, $row)
    {
        $result = ['actors' => []];
        
        // Parse cast string format: "Actor Name:Character Name, Actor Name:Character Name"
        $castMembers = array_map('trim', explode(',', $castString));
        
        foreach ($castMembers as $index => $castMember) {
            if (empty($castMember)) continue;
            
            // Split actor name and character name
            $parts = array_map('trim', explode(':', $castMember));
            $actorName = $parts[0];
            $characterName = $parts[1] ?? 'Actor';
            
            // Find or create actor
            $actor = Actor::firstOrCreate(
                ['fullname' => $actorName],
                [
                    'dob' => '', // Can be updated later
                    'bio' => "Professional actor known for {$content->title}",
                    'profile_image' => null
                ]
            );
            
            // Create content_cast relationship
            ContentCast::firstOrCreate([
                'content_id' => $content->content_id,
                'actor_id' => $actor->actor_id,
                'character_name' => $characterName
            ]);
            
            $result['actors'][] = $actorName;
        }
        
        // Import director as special cast member if provided
        if (!empty($row['director'])) {
            $director = Actor::firstOrCreate(
                ['fullname' => trim($row['director'])],
                [
                    'dob' => '',
                    'bio' => "Director of {$content->title}",
                    'profile_image' => null
                ]
            );
            
            ContentCast::firstOrCreate([
                'content_id' => $content->content_id,
                'actor_id' => $director->actor_id,
                'character_name' => 'Director'
            ]);
        }
        
        return $result;
    }

    /**
     * Import video source for content
     */
    private function importVideoSource($content, $row)
    {
        if (empty($row['video_url'])) {
            return false;
        }
        
        // Determine video type based on URL
        $videoUrl = trim($row['video_url']);
        $type = 4; // Default to MP4
        
        if (strpos($videoUrl, 'youtube.com') !== false || strpos($videoUrl, 'youtu.be') !== false) {
            $type = 1; // YouTube
        } elseif (strpos($videoUrl, '.m3u8') !== false) {
            $type = 2; // M3U8
        } elseif (strpos($videoUrl, '.mov') !== false) {
            $type = 3; // MOV
        } elseif (strpos($videoUrl, '.mkv') !== false) {
            $type = 5; // MKV
        } elseif (strpos($videoUrl, '.webm') !== false) {
            $type = 6; // WebM
        }
        
        $quality = !empty($row['video_quality']) ? trim($row['video_quality']) : 'HD';
        $accessType = !empty($row['access_type']) ? 
            (int)$row['access_type'] : 1; // Default to free
        
        ContentSource::create([
            'content_id' => $content->content_id,
            'title' => $quality . ' Quality',
            'quality' => $quality,
            'size' => !empty($row['video_size']) ? trim($row['video_size']) : null,
            'is_download' => !empty($row['allow_download']) ? 1 : 0,
            'access_type' => $accessType,
            'type' => $type,
            'source' => $videoUrl
        ]);
        
        return true;
    }

    /**
     * Import trailer for content
     */
    private function importTrailer($content, $trailerUrl)
    {
        $youtubeId = null;
        
        // Extract YouTube ID if it's a YouTube URL
        if (preg_match('/(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]+)/', $trailerUrl, $matches)) {
            $youtubeId = $matches[1];
        }
        
        DB::table('content_trailer')->insert([
            'content_id' => $content->content_id,
            'title' => 'Official Trailer',
            'youtube_id' => $youtubeId,
            'trailer_url' => $trailerUrl,
            'is_primary' => 1,
            'sort_order' => 0,
            'created_at' => now(),
            'updated_at' => now()
        ]);
    }

    /**
     * Import age rating for content
     */
    private function importAgeRating($content, $ageRating)
    {
        $ageLimit = AgeLimit::where('code', trim($ageRating))
            ->orWhere('description', 'LIKE', '%' . trim($ageRating) . '%')
            ->first();
        
        if ($ageLimit) {
            DB::table('content_age_limit')->insertOrIgnore([
                'content_id' => $content->content_id,
                'age_limit_id' => $ageLimit->age_limit_id,
                'created_at' => now()
            ]);
        }
    }

    /**
     * Download an enhanced sample CSV template
     */
    public function downloadTemplate()
    {
        $headers = [
            'Content-Type' => 'text/csv',
            'Content-Disposition' => 'attachment; filename="enhanced_content_import_template.csv"',
        ];

        $columns = [
            'title',
            'description',
            'type',
            'duration',
            'release_year',
            'ratings',
            'language',
            'genres',
            'is_featured',
            'vertical_poster',
            'horizontal_poster',
            'trailer_url',
            'age_rating',
            'cast',
            'director',
            'producer',
            'writer',
            'video_url',
            'video_quality',
            'access_type',
            'allow_download'
        ];

        $callback = function() use ($columns) {
            $file = fopen('php://output', 'w');
            
            // Headers
            fputcsv($file, $columns);
            
            // Public domain movies with actual downloadable MP4s
            $sampleData = [
                // 1. Night of the Living Dead (1968) - Public Domain
                [
                    'Night of the Living Dead',
                    'A group of people hide from bloodthirsty zombies in a farmhouse. George A. Romero\'s classic horror film that created the modern zombie genre.',
                    'movie',
                    '96',
                    '1968',
                    '7.9',
                    'English',
                    'Horror, Thriller',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Night_of_the_Living_Dead_%281968%29.jpg/220px-Night_of_the_Living_Dead_%281968%29.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Night_of_the_Living_Dead_%281968%29.jpg/320px-Night_of_the_Living_Dead_%281968%29.jpg',
                    'https://www.youtube.com/watch?v=H91BxkBXttE',
                    'R',
                    'Judith O\'Dea:Barbra, Duane Jones:Ben, Marilyn Eastman:Helen Cooper, Karl Hardman:Harry Cooper',
                    'George A. Romero',
                    'Karl Hardman, Russell Streiner',
                    'John A. Russo, George A. Romero',
                    'https://archive.org/download/night_of_the_living_dead/night_of_the_living_dead_512kb.mp4',
                    'HD',
                    '1',
                    '1'
                ],
                // 2. The General (1926) - Buster Keaton - Public Domain
                [
                    'The General',
                    'During the Civil War, a Southern railroad engineer pursues his stolen locomotive and his girlfriend who has been accidentally taken by Union spies.',
                    'movie',
                    '75',
                    '1926',
                    '8.1',
                    'English',
                    'Action, Adventure, Comedy, War',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/The_General_poster.jpg/220px-The_General_poster.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/The_General_poster.jpg/320px-The_General_poster.jpg',
                    'https://www.youtube.com/watch?v=iHlBMKtgPOA',
                    'G',
                    'Buster Keaton:Johnnie Gray, Marion Mack:Annabelle Lee, Glen Cavender:Captain Anderson, Jim Farley:General Thatcher',
                    'Clyde Bruckman, Buster Keaton',
                    'Joseph M. Schenck',
                    'Al Boasberg, Clyde Bruckman',
                    'https://archive.org/download/The_General_Buster_Keaton/The_General_Buster_Keaton_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ],
                // 3. Nosferatu (1922) - Public Domain
                [
                    'Nosferatu',
                    'Vampire Count Orlok expresses interest in a new residence and real estate agent Hutter\'s wife. F.W. Murnau\'s unauthorized adaptation of Dracula.',
                    'movie',
                    '94',
                    '1922',
                    '7.9',
                    'English',
                    'Horror, Fantasy',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Nosferatu_Eine_Symphonie_des_Grauens_%281922%29.jpg/220px-Nosferatu_Eine_Symphonie_des_Grauens_%281922%29.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Nosferatu_Eine_Symphonie_des_Grauens_%281922%29.jpg/320px-Nosferatu_Eine_Symphonie_des_Grauens_%281922%29.jpg',
                    'https://www.youtube.com/watch?v=FC6jFoYm3xs',
                    'PG',
                    'Max Schreck:Count Orlok, Gustav von Wangenheim:Hutter, Greta Schröder:Ellen Hutter, Alexander Granach:Knock',
                    'F.W. Murnau',
                    'Enrico Dieckmann, Albin Grau',
                    'Henrik Galeen',
                    'https://archive.org/download/Nosferatu_silent_1922/Nosferatu_silent_1922_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ],
                // 4. His Girl Friday (1940) - Public Domain
                [
                    'His Girl Friday',
                    'A newspaper editor uses every trick to keep his ex-wife and star reporter from remarrying in this fast-paced screwball comedy.',
                    'movie',
                    '92',
                    '1940',
                    '7.9',
                    'English',
                    'Comedy, Drama, Romance',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/His_Girl_Friday_%281940_poster%29.jpg/220px-His_Girl_Friday_%281940_poster%29.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/His_Girl_Friday_%281940_poster%29.jpg/320px-His_Girl_Friday_%281940_poster%29.jpg',
                    'https://www.youtube.com/watch?v=WQ3gIJPoBTg',
                    'PG',
                    'Cary Grant:Walter Burns, Rosalind Russell:Hildy Johnson, Ralph Bellamy:Bruce Baldwin, Gene Lockhart:Sheriff Hartwell',
                    'Howard Hawks',
                    'Howard Hawks',
                    'Charles Lederer',
                    'https://archive.org/download/his_girl_friday/his_girl_friday_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ],
                // 5. The Phantom of the Opera (1925) - Public Domain
                [
                    'The Phantom of the Opera',
                    'A disfigured musical genius haunts the Paris Opera House and terrorizes the company for the love of a young soprano.',
                    'movie',
                    '93',
                    '1925',
                    '7.6',
                    'English',
                    'Drama, Horror, Thriller',
                    '0',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/3/35/Phantom_of_the_Opera_1925.jpg/220px-Phantom_of_the_Opera_1925.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/3/35/Phantom_of_the_Opera_1925.jpg/320px-Phantom_of_the_Opera_1925.jpg',
                    'https://www.youtube.com/watch?v=BbkfV1pQcVY',
                    'PG',
                    'Lon Chaney:Erik The Phantom, Mary Philbin:Christine Daaé, Norman Kerry:Vicomte Raoul de Chagny, Arthur Edmund Carewe:Ledoux',
                    'Rupert Julian',
                    'Carl Laemmle',
                    'Gaston Leroux',
                    'https://archive.org/download/phantom_of_the_opera_1925/The_Phantom_of_the_Opera_1925_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ],
                // 6. Charade (1963) - Public Domain
                [
                    'Charade',
                    'A woman pursued by several men who want a fortune her murdered husband had stolen finds help from a charming stranger.',
                    'movie',
                    '113',
                    '1963',
                    '7.9',
                    'English',
                    'Comedy, Mystery, Romance, Thriller',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Charade_movieposter.jpg/220px-Charade_movieposter.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c0/Charade_movieposter.jpg/320px-Charade_movieposter.jpg',
                    'https://www.youtube.com/watch?v=bflfqGFRQdg',
                    'PG',
                    'Cary Grant:Peter Joshua, Audrey Hepburn:Regina Lampert, Walter Matthau:Hamilton Bartholomew, James Coburn:Tex Panthollow',
                    'Stanley Donen',
                    'Stanley Donen',
                    'Peter Stone',
                    'https://archive.org/download/Charade_1963/Charade_1963_512kb.mp4',
                    'HD',
                    '1',
                    '1'
                ],
                // 7. The Little Shop of Horrors (1960) - Public Domain
                [
                    'The Little Shop of Horrors',
                    'A clumsy young man nurtures a plant that feeds on human blood. Roger Corman\'s cult classic black comedy.',
                    'movie',
                    '72',
                    '1960',
                    '6.3',
                    'English',
                    'Comedy, Horror',
                    '0',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Littleshopofhorrors.jpg/220px-Littleshopofhorrors.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Littleshopofhorrors.jpg/320px-Littleshopofhorrors.jpg',
                    'https://www.youtube.com/watch?v=PLKvKM2Ht5o',
                    'PG-13',
                    'Jonathan Haze:Seymour Krelborn, Jackie Joseph:Audrey Fulquard, Mel Welles:Gravis Mushnick, Dick Miller:Burson Fouch',
                    'Roger Corman',
                    'Roger Corman',
                    'Charles B. Griffith',
                    'https://archive.org/download/TheLittleShopOfHorrors1960/The_Little_Shop_of_Horrors_1960_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ],
                // 8. Metropolis (1927) - Public Domain
                [
                    'Metropolis',
                    'In a futuristic city sharply divided between the working class and the city planners, the son of the city\'s mastermind falls in love with a working class prophet.',
                    'movie',
                    '153',
                    '1927',
                    '8.3',
                    'English',
                    'Drama, Sci-Fi',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Metropolis_%281927_film%29.jpg/220px-Metropolis_%281927_film%29.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/0/06/Metropolis_%281927_film%29.jpg/320px-Metropolis_%281927_film%29.jpg',
                    'https://www.youtube.com/watch?v=ZSExdX0tds4',
                    'PG',
                    'Brigitte Helm:Maria / The Machine Man, Alfred Abel:Joh Fredersen, Gustav Fröhlich:Freder, Rudolf Klein-Rogge:Rotwang',
                    'Fritz Lang',
                    'Erich Pommer',
                    'Thea von Harbou',
                    'https://archive.org/download/Metropolis1927Restored/Metropolis_1927_Restored_512kb.mp4',
                    'HD',
                    '1',
                    '1'
                ],
                // 9. D.O.A. (1949) - Public Domain Film Noir
                [
                    'D.O.A.',
                    'A man seeks his own murderer after discovering he\'s been fatally poisoned and has only days to live. Classic film noir.',
                    'movie',
                    '83',
                    '1949',
                    '7.3',
                    'English',
                    'Crime, Drama, Film-Noir, Mystery, Thriller',
                    '0',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/DOA1950.jpg/220px-DOA1950.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/DOA1950.jpg/320px-DOA1950.jpg',
                    'https://www.youtube.com/watch?v=MpPR3GWoUbg',
                    'PG-13',
                    'Edmond O\'Brien:Frank Bigelow, Pamela Britton:Paula Gibson, Luther Adler:Majak, Beverly Garland:Miss Foster',
                    'Rudolph Maté',
                    'Leo C. Popkin',
                    'Russell Rouse, Clarence Greene',
                    'https://archive.org/download/DOA_1949/DOA_1949_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ],
                // 10. The Cabinet of Dr. Caligari (1920) - Public Domain
                [
                    'The Cabinet of Dr. Caligari',
                    'A hypnotist uses a somnambulist to commit murders. German expressionist masterpiece that influenced horror cinema.',
                    'movie',
                    '67',
                    '1920',
                    '8.1',
                    'English',
                    'Horror, Mystery, Thriller',
                    '1',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/The_Cabinet_of_Dr._Caligari_poster.jpg/220px-The_Cabinet_of_Dr._Caligari_poster.jpg',
                    'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/The_Cabinet_of_Dr._Caligari_poster.jpg/320px-The_Cabinet_of_Dr._Caligari_poster.jpg',
                    'https://www.youtube.com/watch?v=IAtpxqajFak',
                    'PG',
                    'Werner Krauss:Dr. Caligari, Conrad Veidt:Cesare, Friedrich Feher:Francis, Lil Dagover:Jane Olsen',
                    'Robert Wiene',
                    'Rudolf Meinert, Erich Pommer',
                    'Carl Mayer, Hans Janowitz',
                    'https://archive.org/download/The_Cabinet_of_Dr_Caligari_1920/The_Cabinet_of_Dr_Caligari_1920_512kb.mp4',
                    'SD',
                    '1',
                    '1'
                ]
            ];
            
            foreach ($sampleData as $row) {
                fputcsv($file, $row);
            }
            
            fclose($file);
        };

        return response()->stream($callback, 200, $headers);
    }
}