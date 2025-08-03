<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Content;
use App\Models\ContentGenre;
use App\Models\Genre;
use App\Models\AppLanguage;
use App\Models\ContentAgeLimit;
use App\Models\AgeLimit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use League\Csv\Reader;
use League\Csv\Statement;
use Exception;

class CsvImportController extends Controller
{
    /**
     * Display the CSV import form
     */
    public function index()
    {
        return view('admin.csv-import.index');
    }

    /**
     * Process the CSV file upload and import content
     */
    public function import(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'csv_file' => 'required|file|mimes:csv,txt|max:10240', // 10MB max
            'dry_run' => 'boolean'
        ]);

        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }

        $isDryRun = $request->get('dry_run', false);
        $results = [
            'success' => 0,
            'errors' => [],
            'warnings' => []
        ];

        try {
            $csv = Reader::createFromPath($request->file('csv_file')->getPathname(), 'r');
            $csv->setHeaderOffset(0);
            
            $headers = $csv->getHeader();
            $requiredHeaders = [
                'title', 'description', 'type', 'duration', 'release_year',
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
                        $this->importRow($row);
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
     * Validate a single CSV row
     */
    private function validateRow($row, $lineNumber)
    {
        $rules = [
            'title' => 'required|string|max:255',
            'description' => 'required|string',
            'type' => 'required|in:movie,series',
            'duration' => 'nullable|integer|min:0',
            'release_year' => 'required|integer|between:1900,' . date('Y'),
            'ratings' => 'nullable|numeric|between:0,10',
            'language' => 'required|string',
            'is_featured' => 'required|in:0,1,yes,no,true,false',
            'vertical_poster' => 'nullable|string|max:255',
            'horizontal_poster' => 'nullable|string|max:255',
            'trailer_url' => 'nullable|url|max:255'
        ];

        $validator = Validator::make($row, $rules);
        
        if ($validator->fails()) {
            throw new Exception($validator->errors()->first());
        }
    }

    /**
     * Import a single row of content
     */
    private function importRow($row)
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
        
        // Create content
        $content = Content::create([
            'title' => trim($row['title']),
            'description' => trim($row['description']),
            'type' => $type,
            'duration' => !empty($row['duration']) ? (int)$row['duration'] : null,
            'release_year' => (int)$row['release_year'],
            'ratings' => !empty($row['ratings']) ? (float)$row['ratings'] : 0,
            'language_id' => $language->app_language_id,
            'trailer_url' => !empty($row['trailer_url']) ? trim($row['trailer_url']) : null,
            'vertical_poster' => !empty($row['vertical_poster']) ? trim($row['vertical_poster']) : null,
            'horizontal_poster' => !empty($row['horizontal_poster']) ? trim($row['horizontal_poster']) : null,
            'genre_ids' => '', // Will be updated below
            'is_featured' => $isFeatured,
            'is_show' => 1,
            'total_view' => 0,
            'total_download' => 0,
            'total_share' => 0
        ]);
        
        // Process genres
        if (!empty($row['genres'])) {
            $genreNames = array_map('trim', explode(',', $row['genres']));
            $genreIds = [];
            
            foreach ($genreNames as $genreName) {
                if (empty($genreName)) continue;
                
                $genre = Genre::firstOrCreate(['title' => $genreName]);
                $genreIds[] = $genre->genre_id;
                
                // Create content_genre relationship
                ContentGenre::firstOrCreate([
                    'content_id' => $content->content_id,
                    'genre_id' => $genre->genre_id
                ]);
            }
            
            // Update genre_ids field (for backward compatibility)
            $content->update(['genre_ids' => implode(',', $genreIds)]);
        }
        
        // Process age limits if provided
        if (!empty($row['age_rating'])) {
            $ageLimit = AgeLimit::where('description', 'LIKE', '%' . trim($row['age_rating']) . '%')->first();
            
            if ($ageLimit) {
                ContentAgeLimit::create([
                    'content_id' => $content->content_id,
                    'age_limit_id' => $ageLimit->age_limit_id
                ]);
            }
        }
        
        return $content;
    }

    /**
     * Download a sample CSV template
     */
    public function downloadTemplate()
    {
        $headers = [
            'Content-Type' => 'text/csv',
            'Content-Disposition' => 'attachment; filename="content_import_template.csv"',
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
            'age_rating'
        ];

        $callback = function() use ($columns) {
            $file = fopen('php://output', 'w');
            
            // Headers
            fputcsv($file, $columns);
            
            // Sample rows
            $sampleData = [
                [
                    'The Great Adventure',
                    'An epic journey through uncharted territories',
                    'movie',
                    '120',
                    '2023',
                    '8.5',
                    'English',
                    'Action, Adventure, Drama',
                    '1',
                    'https://example.com/posters/great-adventure-v.jpg',
                    'https://example.com/posters/great-adventure-h.jpg',
                    'https://youtube.com/watch?v=abc123',
                    'PG-13'
                ],
                [
                    'Mystery Chronicles',
                    'A thrilling detective series',
                    'series',
                    '',
                    '2024',
                    '7.8',
                    'Spanish',
                    'Mystery, Crime, Thriller',
                    '0',
                    'https://example.com/posters/mystery-chronicles-v.jpg',
                    'https://example.com/posters/mystery-chronicles-h.jpg',
                    '',
                    'TV-MA'
                ],
                [
                    'Comedy Central Live',
                    'Stand-up comedy special',
                    'movie',
                    '90',
                    '2024',
                    '6.5',
                    'English',
                    'Comedy',
                    '0',
                    'https://example.com/posters/comedy-central-v.jpg',
                    'https://example.com/posters/comedy-central-h.jpg',
                    'https://vimeo.com/123456',
                    'TV-14'
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