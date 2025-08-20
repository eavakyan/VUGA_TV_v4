<?php

namespace App\Http\Controllers;

use App\Actor;
use App\Constants;
use App\Content;
use App\ContentSource;
use App\Episode;
use App\EpisodeSource;
use App\EpisodeSubtitle;
use App\Category;
use App\GlobalFunction;
use App\Language;
use App\ContentCast;
use App\MediaGallery;
use App\Season;
use App\Subtitle;
use App\TopContent;
use App\User;
use App\Models\V2\ContentDistributor;
use App\Models\V2\AgeLimit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ContentController extends Controller
{
    function fetchHomePageData(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('app_user_id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found'
            ]);
        }

        $userWatchListContent = [];

        $userWatchListContentIds = $user->watchlist_content_ids;
        $featuredContent = Content::where('is_featured', Constants::featured)->where('is_show', Constants::showContent)->get();

        if (!empty($userWatchListContentIds)) {
            $userWatchListContentsArray = explode(',', $userWatchListContentIds);
            $userWatchListContent = Content::where('is_show', Constants::showContent)->whereIn('content_id', $userWatchListContentsArray)->limit(5)->get();
        }

        $topContents = TopContent::whereHas('content',  function ($query){
            $query->where('is_show', Constants::showContent);
        })->with('content')->orderBy('content_index', 'ASC')->get();

        $genres = Category::get();
        $genreContents = [];

        foreach ($genres as $genre) {
            $genreContent = Content::where('is_show', Constants::showContent)
                                    ->whereRaw('FIND_IN_SET(?, genre_ids)', [$genre->category_id])
                                    ->inRandomOrder()
                                    ->limit(env('HOME_PAGE_GENRE_CONTENTS_LIMIT'))
                                    ->get();

            if ($genreContent->isNotEmpty()) {
                $genre->contents = $genreContent;
                $genreContents[] = $genre;
            }
        }



        return response()->json([
            'status' => true,
            'message' => 'Fetch Home Page Data Successfully',
            'featured' => $featuredContent,
            'watchlist' => $userWatchListContent,
            'topContents' => $topContents,
            'genreContents' => $genreContents
        ]);
    }

    function fetchWatchList(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
            'start' => 'required|integer',
            'limit' => 'required|integer',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('app_user_id', $request->user_id)->first();
        if ($user == null) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found'
            ]);
        }

        $userWatchListContent = [];

        $userWatchListContentIds = $user->watchlist_content_ids;

        if (!empty($userWatchListContentIds)) {
            $userWatchListContentsArray = explode(',', $userWatchListContentIds);

            $query = Content::where('is_show', Constants::showContent)->whereIn('content_id', $userWatchListContentsArray);

            if ($request->has('type') && $request->type != 0) {
                $query->where('type', $request->type);
            }

            $userWatchListContent = $query->offset($request->start)->limit($request->limit)->get();
        }

        return response()->json([
            'status' => true,
            'message' => 'Fetch WatchList Successfully',
            'data' => $userWatchListContent,
        ]);
    }

    function fetchContentsByGenre(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'genre_id' => 'required',
            'start' => 'required',
            'limit' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $genre = Category::where('category_id', $request->genre_id)->first();
        if ($genre == null) {
            return response()->json([
                'status' => false,
                'message' => 'Genre Not Found'
            ]);
        }

        $start = $request->start;
        $limit = $request->limit;

        $genreContents = Content::where('is_show', Constants::showContent)
                                ->whereRaw('FIND_IN_SET(?, genre_ids)', [$genre->id])
                                ->skip($start)
                                ->take($limit)
                                ->get();

        return response()->json([
            'status' => true,
            'message' => 'Fetch Contents By Genre',
            'data' => $genreContents
        ]);
    }

    function fetchContentDetails(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'user_id' => 'required',
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $user = User::where('app_user_id', $request->user_id)->first();
        if (!$user) {
            return response()->json([
                'status' => false,
                'message' => 'User Not Found'
            ]);
        }

        $content = Content::with('sources.media')->where('is_show', Constants::showContent)->where('content_id', $request->content_id)->first();
        if (!$content) {
            return response()->json([
                'status' => false,
                'message' => 'Content Not Found'
            ]);
        }

        // WatchList
        $watchlist = false;
        if (!empty($user->watchlist_content_ids)) {
            $watchlistContentIds = explode(',', $user->watchlist_content_ids);
            if (in_array($request->content_id, $watchlistContentIds)) {
                $watchlist = true;
            }
        }
        $content->is_watchlist = $watchlist;

        if ($content->type == Constants::movie) {

            $contentCast = ContentCast::with('actor')->where('content_id', $request->content_id)->get();
            $content->contentCast = $contentCast;

            $content->content_sources = $content->sources;
            $content->content_subtitles = $content->subtitles;
        }

        if ($content->type == Constants::series) {
            $seasons = Season::with([
                                    'episodes', 
                                    'episodes.sources',
                                    'episodes.subtitles'
                                    ])
                                    ->where('content_id', $request->content_id)
                                    ->get();
                                    
            $contentCast = ContentCast::with('actor')->where('content_id', $request->content_id)->get();
            $content->contentCast = $contentCast;
            
            $content->seasons = $seasons;
        }

        // More Like This Contents
        $genreIds = explode(',', $content->genre_ids);
        $moreLikeThis = Content::where('is_show', Constants::showContent)->where(function ($query) use ($genreIds) {
            foreach ($genreIds as $genreId) {
                $query->whereRaw('FIND_IN_SET(?, genre_ids)', [$genreId]);
            }
        })
        ->where('content_id', '!=', $content->content_id)
        ->inRandomOrder()
        ->limit(env('MORE_LIKE_RANDOM_LIST_COUNT'))
        ->get();

        if ($moreLikeThis->isEmpty()) {
            $moreLikeThis = Content::where('is_show', Constants::showContent)->where('type', $content->type)
            ->where('content_id', '!=', $content->content_id)
            ->inRandomOrder()
            ->limit(env('MORE_LIKE_RANDOM_LIST_COUNT'))
            ->get();
        }

        $content->more_like_this = $moreLikeThis;

        unset($content->subtitles);
        unset($content->sources);

        return response()->json([
            'status' => true,
            'message' => 'Fetch Content Successfully',
            'data' => $content
        ]);
    }

    function searchContent(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'start' => 'required',
            'limit' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $query = Content::where('is_show', Constants::showContent)->orderBy('created_at', 'DESC');

        if ($request->has('type')) {
            $query->where('type', $request->type);
        }
        
        if ($request->has('genre_id')) {
            $query->whereRaw('FIND_IN_SET(?, genre_ids)', [$request->genre_id]);
        }
        
        if ($request->has('language_id')) {
            $query->where('language_id', $request->language_id);
        }
        
        if ($request->has('keyword')) {
            $query->where('title', 'LIKE', '%' . $request->keyword . '%');
        }

        $contents = $query->offset($request->start)
                            ->limit($request->limit)
                            ->get();

        return response()->json([
            'status' => true,
            'message' => 'Content Result',
            'data' => $contents
        ]);

    }
    
    function increaseContentView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $content = Content::where('content_id', $request->content_id)->first();
        $content->total_view += 1;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase Content View Successfully',
            'data' => $content
        ]);

    }

    function increaseContentDownload(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $content = Content::where('content_id', $request->content_id)->first();
        $content->total_download += 1;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase Content View Successfully',
            'data' => $content
        ]);
    }

    function increaseContentShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $content = Content::where('content_id', $request->content_id)->first();
        $content->total_share += 1;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase Content View Successfully',
            'data' => $content
        ]);
    }

    function increaseEpisodeView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $episode = Episode::where('episode_id', $request->episode_id)->first();
        $episode->total_view += 1;
        $episode->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase Episode View Successfully',
            'data' => $episode
        ]);
    }

    function increaseEpisodeDownload(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'episode_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $episode = Episode::where('episode_id', $request->episode_id)->first();
        $episode->total_download += 1;
        $episode->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase Episode View Successfully',
            'data' => $episode
        ]);
    }

    // web
    public function contentList()
    {
        $genres = Category::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        $movieCount = Content::where('type', Constants::movie)->count();
        $seriesCount = Content::where('type', Constants::series)->count();

        $TMDBAPI = ['TMDB_API_KEY' => env('TMDB_API_KEY')];
        return view('contentList', compact('TMDBAPI', 'genres', 'languages', 'movieCount', 'seriesCount'));
    }

    public function fetchMoviesList(Request $request)
    {
        $content_type = Constants::movie;
        $columns = ['content_id'];

        $query = Content::where('type', $content_type);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue) {
                $q->where('title', 'LIKE', "%{$searchValue}%")
                ->orWhere('description', 'LIKE', "%{$searchValue}%")
                ->orWhere('ratings', 'LIKE', "%{$searchValue}%")
                ->orWhere('release_year', 'LIKE', "%{$searchValue}%")
                ->orWhereHas('language', function ($q) use ($searchValue) {
                    $q->where('title', 'LIKE', "%{$searchValue}%");
                });
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
                        ->offset($start)
                        ->limit($limit)
                        ->get();

        $data = $result->map(function ($item) {

        $verticalImageUrl = $item->vertical_poster ?  $item->vertical_poster : './assets/img/default.png';
        $horizontalImageUrl = $item->horizontal_poster ?  $item->horizontal_poster : './assets/img/default.png';

        $horizontalPoster = "<img data-fancybox src='{$verticalImageUrl}' alt='image' class='object-cover img-fluid vertical_poster_tbl img-border border-radius'>
                            <img data-fancybox src='{$horizontalImageUrl}' alt='image' class='object-cover img-fluid horizontal_poster_tbl img-border border-radius'>";

            $featured = $item->is_featured == Constants::featured
                ? '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none unfeatured" checked rel="' . $item->content_id . '" value="' . $item->is_featured . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
               </div>'
                : '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none featured" rel="' . $item->content_id . '" value="' . $item->is_featured . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
               </div>';


            $activeContent = $item->is_show == Constants::showContent
                ? '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none hideContent" checked rel="' . $item->content_id . '" value="' . $item->is_show . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
               </div>'
                : '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none showContent" rel="' . $item->content_id . '" value="' . $item->is_show . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
               </div>';

            $movieDetail = "<a href='contentList/{$item->content_id}' class='btn btn-info me-2 shadow-none text-white' style='white-space: nowrap;'>" . __('details') . "</a>";

            $edit = "<a rel='{$item->content_id}'
                    data-type='{$item->type}' 
                    data-title='{$item->title}'
                    data-description='{$item->description}' 
                    data-duration='{$item->duration}'
                    data-release_year='{$item->release_year}' 
                    data-ratings='{$item->ratings}' 
                    data-language_id='{$item->language_id}' 
                    data-genre_ids='{$item->genre_ids}' 
                    data-trailer_url='{$item->trailer_url}'  
                    data-vposter='{$item->vertical_poster}' 
                    data-hposter='{$item->horizontal_poster}' 
                    class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->content_id}'>" . __('delete') . "</a>";
            $notifyContent = "<a href='#' class='ms-2 text-white notifyContent btn btn-warning shadow-none' style='padding: 6px 10px !important;' rel='{$item->content_id}' data-title='{$item->title}' data-description='{$item->description}' '><svg viewBox='0 0 24 24' width='24' height='24' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round' class='css-i6dzq1'><path d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'></path><path d='M13.73 21a2 2 0 0 1-3.46 0'></path></svg></a>";

            $action = "<div class='text-end action'>{$movieDetail}{$edit}{$delete}{$notifyContent}</div>";

            return [
                $horizontalPoster,
                $item->title,
                $item->ratings,
                number_format($item->total_view),
                $item->release_year,
                $item->language->title,
                $featured,
                $activeContent,
                $action,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function unfeatured(Request $request)
    {
        $content = Content::where('content_id', $request->content_id)->first();
        $content->is_featured = Constants::unfeatured;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Content Removed From Featured',
        ]);
    }

    public function featured(Request $request)
    {
        $content = Content::where('content_id', $request->content_id)->first();
        $content->is_featured = Constants::featured;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Content Added in Featured',
        ]);
    }

    public function hideContent(Request $request)
    {
        $content = Content::where('content_id', $request->content_id)->first();
        $content->is_show = Constants::hideContent;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Content is hide',
        ]);
    }

    public function showContent(Request $request)
    {
        $content = Content::where('content_id', $request->content_id)->first();
        $content->is_show = Constants::showContent;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Content is Show',
        ]);
    }

    public function addNewContent(Request $request)
    {
        $content = new Content();
        $content->type = $request->type;
        $content->title = $request->title;
        $content->description = $request->description;
        $content->duration = $request->duration;
        $content->release_year = $request->release_year;
        $content->ratings = $request->ratings;
        $content->language_id = $request->language_id;
        $content->genre_ids = implode(',', $request->genre_ids);

        // Handle vertical poster
        if ($request->hasFile('vertical_poster')) {
            $verticalPoster = $request->file('vertical_poster');
            $verticalPosterPath = GlobalFunction::saveFileAndGivePath($verticalPoster);
        } elseif ($request->has('vertical_poster_url')) {
            $verticalPosterPath = GlobalFunction::saveImageFromUrl($request->vertical_poster_url);
        } else {
            $verticalPosterPath = null;
        }
        $content->vertical_poster = $verticalPosterPath;

        // Handle horizontal poster
        if ($request->hasFile('horizontal_poster')) {
            $horizontalPoster = $request->file('horizontal_poster');
            $horizontalPosterPath = GlobalFunction::saveFileAndGivePath($horizontalPoster);
        } elseif ($request->has('horizontal_poster_url')) {
            $horizontalPosterPath = GlobalFunction::saveImageFromUrl($request->horizontal_poster_url);
        } else {
            $horizontalPosterPath = null;
        }
        $content->horizontal_poster = $horizontalPosterPath;

        $content->save();

        // Handle trailers for new content
        if ($request->has('trailers')) {
            $this->updateContentTrailers($content->content_id, $request->trailers);
        }

        return response()->json([
            'status' => true,
            'message' => 'Content Added Successfully',
            'data' => $content,
        ]);
    }

    public function updateContent(Request $request)
    {
        $content = Content::where('content_id', $request->content_id)->first();
        if ($content == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        $content->title = $request->title;
        $content->description = $request->description;
        $content->duration = $request->duration;
        $content->release_year = $request->release_year;
        $content->ratings = $request->ratings;
        $content->language_id = $request->language_id;
        $content->genre_ids = implode(',', $request->genre_ids);
        $content->content_distributor_id = $request->content_distributor_id;
        
        // Handle age limits
        if ($request->has('age_limit_ids')) {
            // Delete existing age limits for this content
            \DB::table('content_age_limit')->where('content_id', $content->content_id)->delete();
            
            // Add new age limits
            if (is_array($request->age_limit_ids) && count($request->age_limit_ids) > 0) {
                foreach ($request->age_limit_ids as $ageLimitId) {
                    \DB::table('content_age_limit')->insert([
                        'content_id' => $content->content_id,
                        'age_limit_id' => $ageLimitId,
                        'created_at' => now(),
                        'updated_at' => now(),
                    ]);
                }
            }
        }
        
        // Handle trailers
        if ($request->has('trailers')) {
            $this->updateContentTrailers($content->content_id, $request->trailers);
        }

        if ($request->hasFile('vertical_poster')) {
            GlobalFunction::deleteFile($content->vertical_poster);
            
            $verticalPoster = $request->file('vertical_poster');
            $verticalPosterPath = GlobalFunction::saveFileAndGivePath($verticalPoster);
            $content->vertical_poster = $verticalPosterPath;
        }
        if ($request->hasFile('horizontal_poster')) {
            GlobalFunction::deleteFile($content->horizontal_poster);
            
            $horizontalPoster = $request->file('horizontal_poster');
            $horizontalPosterPath = GlobalFunction::saveFileAndGivePath($horizontalPoster);
            $content->horizontal_poster = $horizontalPosterPath;
        }
        
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Content Update Successfully',
        ]);

    }

    public function deleteContent(Request $request)
    {
        $content = Content::where('content_id', $request->content_id)->first();

        if (!$content) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
                'data' => $content,
            ]);
        }

        if ($content->type == Constants::movie) {
            // Delete content sources
            foreach ($content->sources as $source) {
                GlobalFunction::deleteFile($source->source);
                $source->delete();
            }

            // Delete content cast
            foreach ($content->casts as $cast) {
                $cast->delete();
            }

            // Delete content subtitles
            foreach ($content->subtitles as $subtitle) {
                GlobalFunction::deleteFile($subtitle->file);
                $subtitle->delete();
            }
        } else {
            // Delete seasons, episodes, sources, and subtitles
            foreach ($content->seasons as $season) {
                foreach ($season->episodes as $episode) {
                    foreach ($episode->sources as $source) {
                        if ($source->type == Constants::FileType) {
                            GlobalFunction::deleteFile($source->source);
                        }
                        $source->delete();
                    }

                    foreach ($episode->subtitles as $subtitle) {
                        GlobalFunction::deleteFile($subtitle->file);
                        $subtitle->delete();
                    }

                    GlobalFunction::deleteFile($episode->thumbnail);
                    $episode->delete();
                }
                $season->delete();
            }
        }

        // Delete content posters
        GlobalFunction::deleteFile($content->vertical_poster);
        GlobalFunction::deleteFile($content->horizontal_poster);

        TopContent::where('content_id', $request->content_id)->delete();

        // Delete the content
        $content->delete();

        return response()->json([
            'status' => true,
            'message' => 'Content Deleted Successfully',
        ]);
    }

    public function contentDetailView(Request $request)
    {
        $content = Content::with('trailers')->where('content_id', $request->id)->where('type', Constants::movie)->first();
        if ($content == null) {
            return response()->json([
                'status' => false,
                'message' => 'Content Not Found',
            ]);
        }

        $actors = Actor::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        $genres = Category::orderBy('created_at', 'DESC')->get();
        $mediaGalleries = MediaGallery::orderBy('created_at', 'DESC')->get();
        $distributors = ContentDistributor::where('is_active', 1)->orderBy('name', 'ASC')->get();
        $ageLimits = AgeLimit::orderBy('min_age', 'ASC')->get();
        
        // Get current age limits for this content
        $contentAgeLimits = [];
        if ($content) {
            $contentAgeLimits = \DB::table('content_age_limit')
                ->where('content_id', $content->content_id)
                ->pluck('age_limit_id')
                ->toArray();
        }
        
        // Debug: Log the distributors count
        \Log::info('Distributors count: ' . $distributors->count());

        return view('movieDetail', [
            'content' => $content,
            'actors' => $actors,
            'languages' => $languages,
            'genres' => $genres,
            'mediaGalleries' => $mediaGalleries,
            'distributors' => $distributors,
            'ageLimits' => $ageLimits,
            'contentAgeLimits' => $contentAgeLimits,
        ]);
    }

    public function fetchSourceList(Request $request)
    {
        $contentId = $request->input('content_id');
        $columns = ['content_id'];

        $typeMappings = [
            "Youtube" => 1,
            "M3u8 Url" => 2,
            "Mov Url" => 3,
            "Mp4 Url" => 4,
            "Mkv Url" => 5,
            "Webm Url" => 6,
            "File" => 7,
        ];

        $query = ContentSource::where('content_id', $contentId);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue, $typeMappings) {
                $q->where('title', 'LIKE', "%{$searchValue}%")
                ->orWhere('quality', 'LIKE', "%{$searchValue}%");
                if (array_key_exists($searchValue, $typeMappings)) {
                    $q->orWhere('type', $typeMappings[$searchValue]);
                }
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
                        ->offset($start)
                        ->limit($limit)
                        ->get();

        $data = $result->map(function ($item) use ($typeMappings) {
            
            if ($item->type == Constants::FileType) {
                $source = '<a href="javascript:;" 
                            rel="' . $item->content_source_id . '"  
                            data-source_url="' . $item->media->file . '" 
                            class="me-2 btn btn-primary px-4 text-white source_file_video">' . __('videoPreview') . ' </a>';
            } elseif ($item->type == Constants::Youtube) {
                $sourceUrl = 'https://youtu.be/' . $item->source;
                $source = '<a href="' . $sourceUrl . '" target="_blank" class="sourceUrlLink"> ' . __('preview') . ' </a>';
            } else {
                $source = '<a href="' . $item->source . '" target="_blank" class="sourceUrlLink"> ' . __('preview') . ' </a>';
            }

            $edit = '<a rel="' . $item->content_source_id . '"
                        data-title="' . $item->title . '" 
                        data-quality="' . $item->quality . '" 
                        data-size="' . $item->size . '" 
                        data-download="' . $item->is_download . '" 
                        data-accesstype="' . $item->access_type . '"
                        data-type="' . $item->type . '" 
                        data-source="' . $item->source . '" 
                        class="me-2 btn btn-success px-3 text-white edit">' . __('edit') . '</a>';

            $delete = '<a href="#" class="btn btn-danger px-3 text-white delete" rel=' . $item->content_source_id . '>' . __('delete') . '</a>';

            $action = '<div class="text-end action">' . $edit . $delete . '</div>';

            $typeName = array_search($item->type, $typeMappings) ?: "File";

            return [
                $item->title,
                $typeName,
                $source,
                $item->quality,
                $action,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function addSource(Request $request)
    {
        $source = new ContentSource();
        $source->content_id = $request->content_id;
        $source->title = $request->title;
        $source->quality = $request->quality;
        $source->size = $request->size;
        $source->is_download = $request->is_download;
        $source->type = $request->type;
        $source->access_type = $request->access_type;

        if ($request->has('media')) {
            $source->source = $request->media;
        } else {
            $source->source = $request->source_url;
        }
        $source->save();

        return response()->json([
            'status' => true,
            'message' => 'Source Added Successfully',
        ]);

    }

    public function updateContentSource(Request $request)
    {
        $contentSource = ContentSource::where('content_source_id', $request->source_id)->first();
        if ($contentSource == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        if ($contentSource->type != $request->type && $contentSource->type == Constants::FileType && $contentSource->source != null) {
            GlobalFunction::deleteFile($contentSource->source);
        }

        $contentSource->title = $request->title;
        $contentSource->quality = $request->quality;
        $contentSource->size = $request->size;
        $contentSource->is_download = $request->is_download;
        $contentSource->type = $request->type;
        $contentSource->access_type = $request->access_type;

        if ($request->type == Constants::FileType) {
            $contentSource->source = $request->media;
        } elseif ($request->filled('source_url')) {
            $contentSource->source = $request->source_url;
        }

        $contentSource->save();

        return response()->json([
            'status' => true,
            'message' => 'Content Update Successfully',
        ]);
    }
    
    private function updateContentTrailers($contentId, $trailersJson)
    {
        $trailers = json_decode($trailersJson, true);
        if (!$trailers) return;
        
        // Delete existing trailers
        \App\Models\V2\ContentTrailer::where('content_id', $contentId)->delete();
        
        // Add new trailers
        foreach ($trailers as $index => $trailerData) {
            $trailer = new \App\Models\V2\ContentTrailer();
            $trailer->content_id = $contentId;
            $trailer->title = $trailerData['title'] ?? 'Trailer';
            
            // Extract YouTube ID if it's a YouTube URL, otherwise leave null
            $youtubeId = \App\Models\V2\ContentTrailer::extractYouTubeId($trailerData['url']);
            $trailer->youtube_id = $youtubeId; // This can be null for non-YouTube URLs
            
            $trailer->trailer_url = $trailerData['url'];
            $trailer->is_primary = $trailerData['is_primary'] ? 1 : 0;
            $trailer->sort_order = $index;
            $trailer->save();
        }
    }
    
    public function getContentTrailers($id)
    {
        $trailers = \App\Models\V2\ContentTrailer::where('content_id', $id)
                    ->orderBy('sort_order')
                    ->orderBy('content_trailer_id')
                    ->get();
        
        return response()->json([
            'status' => true,
            'data' => $trailers
        ]);
    }

    public function deleteSource(Request $request)
    {
        $contentSource = ContentSource::where('content_source_id', $request->source_id)->first();
        if ($contentSource == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        GlobalFunction::deleteFile($contentSource->source);

        $contentSource->delete();

        return response()->json([
            'status' => true,
            'message' => 'Content Source Deleted Successfully',
        ]);
    }

    public function fetchCastList(Request $request)
    {
        $contentId = $request->input('content_id');
        $columns = ['content_id'];

        $query = ContentCast::where('content_id', $contentId);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue) {
                $q->whereHas('actor', function ($actorQuery) use ($searchValue) {
                    $actorQuery->where('fullname', 'LIKE', "%{$searchValue}%");
                })->orWhere('character_name', 'LIKE', "%{$searchValue}%");
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->with('actor')
            ->get();

        $data = $result->map(function ($item) {
            $image = "<div class='d-flex align-items-center'>
                    <img data-fancybox src='{$item->actor->profile_image}' class='object-fit-cover border-radius' width='60px' height='60px'>
                    <span class='ms-3'>{$item->actor->fullname}</span>
                </div>";

            $characterName = $item->character_name;

            $edit = "<a rel='{$item->content_cast_id}'
                    data-actor_id='{$item->actor->actor_id}' 
                    data-character_name='{$item->character_name}' 
                    class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->content_cast_id}'>" . __('delete') . "</a>";

            $action = "<div class='text-end action'>{$edit}{$delete}</div>";

            return [
                $image,
                $characterName,
                $action,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function addCast(Request $request)
    {
        $contentCast = new ContentCast();
        $contentCast->content_id = $request->content_id;
        $contentCast->actor_id = $request->actor_id;
        $contentCast->character_name = $request->character_name;

        $contentCast->save();
        
        return response()->json([
            'status' => true,
            'message' => 'Movie Cast Added Successfully',
        ]);
    }

    public function updateCast(Request $request)
    {
        $contentCast = ContentCast::where('content_cast_id', $request->cast_id)->first();
        if ($contentCast == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $contentCast->actor_id = $request->actor_id;
        $contentCast->character_name = $request->character_name;

        $contentCast->save();

        return response()->json([
            'status' => true,
            'message' => 'Movie Cast Updated Successfully',
            'data' => $contentCast,
        ]);
    }

    public function deleteCast(Request $request)
    {
        $contentCast = ContentCast::where('content_cast_id', $request->cast_id)->first();
        if ($contentCast == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $contentCast->delete();

        return response()->json([
            'status' => true,
            'message' => 'Movie Cast Deleted Successfully',
            'data' => $contentCast,
        ]);
    }

    public function fetchSubtitleList(Request $request)
    {
        $contentId = $request->input('content_id');
        $columns = ['content_id'];

        $query = Subtitle::where('content_id', $contentId)->with('language');
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->whereHas('language', function ($languageQuery) use ($searchValue) {
                $languageQuery->where('title', 'LIKE', "%{$searchValue}%");
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $download = '<a download href="' . $item->file . '" class="me-2 btn btn-info px-3 text-white download shadow-none">' . __('download') . '</a>';

            $delete = '<a href="#" class="btn btn-danger px-3 text-white delete" rel="' . $item->subtitle_id . '">' . __('delete') . '</a>';

            $action = '<div class="text-end action">' . $download . $delete . '</div>';

            return [
                $item->language->title,
                $action,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function addSubtitle(Request $request)
    {

        $subtitle = new Subtitle();
        $subtitle->content_id = $request->content_id;
        $subtitle->language_id = $request->language_id;

        if ($request->hasFile('file')) {
            $subtitleFile = $request->file('file');
            $subtitleFilePath = GlobalFunction::saveSubtitleFileAsSrt($subtitleFile);
            $subtitle->file = $subtitleFilePath;
        }
        $subtitle->save();

        return response()->json([
            'status' => true,
            'message' => 'Subtitle Added Successfully',
            'data' => $subtitle,
        ]);
    }

    public function deleteSubtitle(Request $request)
    {
        $subtitle = Subtitle::where('subtitle_id', $request->subtitle_id)->first();
        if ($subtitle == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
         
        GlobalFunction::deleteFile($subtitle->file);

        $subtitle->delete();

        return response()->json([
            'status' => true,
            'message' => 'Subtitle Deleted Successfully',
            'data' => $subtitle,
        ]);
    }

    // Series
    public function fetchSeriesList(Request $request)
    {
        $content_type = Constants::series;
        $columns = ['content_id'];

        $query = Content::where('type', $content_type);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue) {
                $q->where('title', 'LIKE', "%{$searchValue}%")
                ->orWhere('description', 'LIKE', "%{$searchValue}%")
                ->orWhere('ratings', 'LIKE', "%{$searchValue}%")
                ->orWhere('release_year', 'LIKE', "%{$searchValue}%")
                ->orWhereHas('language', function ($q) use ($searchValue) {
                    $q->where('title', 'LIKE', "%{$searchValue}%");
                });
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            
            $horizontalPoster = "<img data-fancybox src='{$item->vertical_poster}' alt='image' class='object-cover img-fluid vertical_poster_tbl img-border border-radius'>
                            <img data-fancybox src='{$item->horizontal_poster}' alt='image' class='object-cover img-fluid horizontal_poster_tbl img-border border-radius'>";

            $featured = $item->is_featured == Constants::featured
                ? '<div class="checkbox-slider d-flex align-items-center">
                <label>
                    <input type="checkbox" class="d-none unfeatured" checked rel="' . $item->id . '" value="' . $item->is_featured . '" >
                    <span class="toggle_background">
                        <div class="circle-icon"></div>
                        <div class="vertical_line"></div>
                    </span>
                </label>
               </div>'
                : '<div class="checkbox-slider d-flex align-items-center">
                <label>
                    <input type="checkbox" class="d-none featured" rel="' . $item->id . '" value="' . $item->is_featured . '" >
                    <span class="toggle_background">
                        <div class="circle-icon"></div>
                        <div class="vertical_line"></div>
                    </span>
                </label>
               </div>';

            $activeContent = $item->is_show == Constants::showContent
                ? '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none hideContent" checked rel="' . $item->content_id . '" value="' . $item->is_show . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
               </div>'
                : '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none showContent" rel="' . $item->content_id . '" value="' . $item->is_show . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
               </div>';


            $seriesDetail = "<a href='series/{$item->content_id}' class='btn btn-info me-2 shadow-none text-white' style='white-space: nowrap;'>" . __('details') . "</a>";
            $title = "<span class='itemDescription'> $item->title </span>";

            $edit = "<a rel='{$item->content_id}'
                data-type='{$item->type}' 
                data-title='{$item->title}'
                data-description='{$item->description}' 
                data-duration='{$item->duration}'
                data-release_year='{$item->release_year}' 
                data-ratings='{$item->ratings}' 
                data-language_id='{$item->language_id}' 
                data-genre_ids='{$item->genre_ids}' 
                data-trailer_url='{$item->trailer_url}'  
                data-vposter='{$item->vertical_poster}' 
                data-hposter='{$item->horizontal_poster}' 
                class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->content_id}'>" . __('delete') . "</a>";
            $notifyContent = "<a href='#' class='ms-2 text-white notifyContent btn btn-warning shadow-none' style='padding: 6px 10px !important;' rel='{$item->content_id}' data-title='{$item->title}' data-description='{$item->description}' '><svg viewBox='0 0 24 24' width='24' height='24' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round' class='css-i6dzq1'><path d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'></path><path d='M13.73 21a2 2 0 0 1-3.46 0'></path></svg></a>";


            $action = "<div class='text-end action'>{$seriesDetail}{$edit}{$delete}{$notifyContent}</div>";

            return [
                $horizontalPoster,
                $title,
                $item->ratings,
                number_format($item->total_view),
                $item->release_year,
                $item->language->title,
                $featured,
                $activeContent,
                $action,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function seriesDetailView(Request $request)
    {
        $content = Content::with('trailers')->where('content_id', $request->id)->where('type', Constants::series)->first();
        if ($content == null) {
            return response()->json([
                'status' => false,
                'message' => 'Content Not Found',
            ]);
        }

        $actors = Actor::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        $seasons = Season::where('content_id', $request->id)->get();
        $genres = Category::orderBy('created_at', 'DESC')->get();
        $distributors = ContentDistributor::where('is_active', 1)->orderBy('name', 'ASC')->get();
        $ageLimits = AgeLimit::orderBy('min_age', 'ASC')->get();
        
        // Get current age limits for this content
        $contentAgeLimits = [];
        if ($content) {
            $contentAgeLimits = \DB::table('content_age_limit')
                ->where('content_id', $content->content_id)
                ->pluck('age_limit_id')
                ->toArray();
        }

        return view('seriesDetail', [
            'content' => $content,
            'actors' => $actors,
            'languages' => $languages,
            'seasons' => $seasons,
            'genres' => $genres,
            'distributors' => $distributors,
            'ageLimits' => $ageLimits,
            'contentAgeLimits' => $contentAgeLimits,
        ]);
    }

    public function addSeason(Request $request)
    {

        $season = new Season();
        $season->content_id = $request->content_id;
        $season->title = $request->title;
        $season->trailer_url = $request->trailer_url;
        $season->save();

        return response()->json([
            'status' => true,
            'message' => 'Season Added Successfully',
            'data' => $season,
        ]);
    }

    public function updateSeason(Request $request)
    {
        $season = Season::where('season_id', $request->season_id)->first();
        if ($season == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $season->title = $request->title;
        $season->trailer_url = $request->trailer_url;
        $season->save();

        return response()->json([
            'status' => true,
            'message' => 'Season Updated Successfully',
            'data' => $season,
        ]);
    }
    
    public function deleteSeason(Request $request)
    {
        $season = Season::find($request->season_id);

        if (!$season) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        foreach ($season->episodes as $episode) {
            
            foreach ($episode->sources as $source) {
                if ($source->type == Constants::FileType) {
                    GlobalFunction::deleteFile($source->source);
                }
                $source->delete();
            }

          
            foreach ($episode->subtitles as $subtitle) {
                GlobalFunction::deleteFile($subtitle->file);
                $subtitle->delete();
            }

           
            GlobalFunction::deleteFile($episode->thumbnail);
            $episode->delete();
        }

        $season->delete();

        return response()->json([
            'status' => true,
            'message' => 'Season Deleted Successfully',
        ]);
    }

    public function fetchEpisodeList(Request $request)
    {
        $seasonId = $request->season_id;
        $columns = ['content_id'];

        $query = Episode::orderBy('number', 'ASC')->where('season_id', $seasonId);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue) {
                $q->where('title', 'LIKE', "%{$searchValue}%")
                ->orWhere('description', 'LIKE', "%{$searchValue}%");
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $thumbnailPoster = '<span class="me-2">' . $item->number . '</span>' . '<img data-fancybox src="' . $item->thumbnail . '" alt="vertical image" class="object-cover img-fluid horizontal_poster_tbl">';
            $itemDescription = '<span class="itemDescription">' . $item->description . '</span>';
            $episodeDetail = '<a href="episodeDetail/' . $item->season_id . '" class="btn btn-info me-2 shadow-none text-white" style="white-space: nowrap;">' . __('episodeDetail') . '</a>';
            $edit = '<a rel="' . $item->season_id . '"
                    data-number="' . $item->number . '"
                    data-thumbnail="' . $item->thumbnail . '"
                    data-title="' . $item->title . '"
                    data-description="' . $item->description . '" 
                    data-duration="' . $item->duration . '" 
                    data-access_type="' . $item->access_type . '" 
                    class="me-2 btn btn-success px-3 text-white edit">' . __('edit') . '</a>';
            $delete = '<a href="#" class="btn btn-danger px-3 text-white delete" rel="' . $item->season_id . '">' . __('delete') . '</a>';
            $action = '<div class="text-end action">' . $episodeDetail . $edit . $delete . '</div>';

            return [
                $thumbnailPoster,
                $item->title,
                $itemDescription,
                $action,
            ];
        });

        // Prepare the JSON response
        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function addEpisode(Request $request)
    {
        $episode = new Episode();
        $episode->season_id = $request->season_id;
        $episode->number = $request->number;

        $thumbnail = $request->file('thumbnail');
        $thumbnailPath = GlobalFunction::saveFileAndGivePath($thumbnail);
        $episode->thumbnail = $thumbnailPath;

        $episode->title = $request->title;
        $episode->description = $request->description;
        $episode->duration = $request->duration;
        
        $episode->save();

        return response()->json([
            'status' => true,
            'message' => 'Episode Added Successfully',
            'data' => $episode,
        ]);
    }

    public function updateEpisode(Request $request)
    {
        $episode = Episode::where('episode_id', $request->episode_id)->first();
        if ($episode == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $episode->number = $request->number;

        if ($request->hasFile('thumbnail')) {
            GlobalFunction::deleteFile($episode->thumbnail);
            $filePath = GlobalFunction::saveFileAndGivePath($request->file('thumbnail'));
            $episode->thumbnail = $filePath;
        }

        $episode->title = $request->title;
        $episode->description = $request->description;
        $episode->duration = $request->duration;
        $episode->save();

        return response()->json([
            'status' => true,
            'message' => 'Episode Updated Successfully',
            'data' => $episode,
        ]);
    }

    public function deleteEpisode(Request $request)
    {
        $episode = Episode::with(['sources', 'subtitles'])->find($request->episode_id);

        if (!$episode) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
 
        GlobalFunction::deleteFile($episode->thumbnail);
 
        foreach ($episode->sources as $episodeSource) {
            GlobalFunction::deleteFile($episodeSource->source);
            $episodeSource->delete();
        }
 
        foreach ($episode->subtitles as $episodeSubtitle) {
            GlobalFunction::deleteFile($episodeSubtitle->file);
            $episodeSubtitle->delete();
        }
        
        $season = Season::where('id', $episode->season_id)->first();
 
        $episode->delete();

        return response()->json([
            'status' => true,
            'message' => 'Episode Deleted Successfully',
            'content_id' => $season->content_id,

        ]);
    }

    public function episodeDetailView(Request $request)
    {
        $episode = Episode::where('episode_id', $request->id)->first();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        $mediaGalleries = MediaGallery::orderBy('created_at', 'DESC')->get();

        if ($episode == null) {
            return response()->json([
                'status' => false,
                'message' => 'Episode Not Found',
            ]);
        }

        return view('episodeDetail', [
            'episode' => $episode,
            'languages' => $languages,
            'mediaGalleries' => $mediaGalleries,
        ]);
    }

    public function fetchEpisodeSourceList(Request $request)
    {

        $totalData = EpisodeSource::where('episode_id', $request->episode_id)->count();
        $rows = EpisodeSource::where('episode_id', $request->episode_id)->orderBy('id', 'DESC')->get();

        $result = $rows;

        $columns = array(
            0 => 'id'
        );

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');


        $totalFiltered = $totalData;
        if (empty($request->input('search.value'))) {
            $result = EpisodeSource::offset($start)
                ->where('episode_id', $request->episode_id)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
        } else {
            $search = $request->input('search.value');
            $result =  EpisodeSource::where('episode_id', $request->episode_id)
                ->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();

            $totalFiltered = $result->count();
        }

        $data = array();
        foreach ($result as $item) {
            if ($item->type == Constants::FileType) {
                $source = '<a href="javascript:;" 
                    rel="' . $item->episode_source_id . '"  
                    data-source_url="' . $item->source . '" 
                    class="me-2 btn btn-primary px-4 text-white source_file_video"> Video Preview </a>';
            } else if ($item->type == Constants::Youtube) {
                $sourceUrl = 'https://youtu.be/' . $item->source;
                $source = '<a href="' . $sourceUrl . '" target="_blank" class="sourceUrlLink"> Preview </a>';
            } else {
                $source = '<a href="' . $item->source . '" target="_blank" class="sourceUrlLink"> Preview </a>';
            }

            $edit = '<a rel="' . $item->episode_source_id . '"
                        data-title="' . $item->title . '" 
                        data-quality="' . $item->quality . '" 
                        data-size="' . $item->size . '" 
                        data-download="' . $item->is_download . '" 
                        data-accesstype="' . $item->access_type . '"
                        data-type="' . $item->type . '" 
                        data-source="' . $item->source . '" 
                        class="me-2 btn btn-success px-3 text-white edit">' . __('edit') . '</a>';

            $delete = '<a href="#" class="btn btn-danger px-3 text-white delete" rel=' . $item->episode_source_id . ' >' . __('delete') . '</a>';

            $action = '<div class="text-end action"> '  . $edit . $delete . ' </div>';

            switch ($item->type) {
                case 1:
                    $item->type = "Youtube";
                    break;
                case 2:
                    $item->type = "M3u8 Url";
                    break;
                case 3:
                    $item->type = "Mov Url";
                    break;
                case 4:
                    $item->type = "Mp4 Url";
                    break;
                case 5:
                    $item->type = "Mkv Url";
                    break;
                case 6:
                    $item->type = "Webm Url";
                    break;
                default:
                    $item->type = "File";
                    break;
            }

            $data[] = array(
                $item->title,
                $item->type,
                $source,
                $item->quality,
                $action,
            );
        }

        $json_data = array(
            "draw"            => intval($request->input('draw')),
            "recordsTotal"    => intval($totalData),
            "recordsFiltered" => $totalFiltered,
            "data"            => $data
        );
        echo json_encode($json_data);
        exit();
    }

    public function addEpisodeSource(Request $request)
    {
        $source = new EpisodeSource();
        $source->episode_id = $request->episode_id;
        $source->title = $request->title;
        $source->quality = $request->quality;
        $source->size = $request->size;
        $source->is_download = $request->is_download;
        $source->type = $request->type;
        $source->access_type = $request->access_type;

        if ($request->has('media')) {
            $source->source = $request->media;
        } else {
            $source->source = $request->source_url;
        }

        $source->save();

        return response()->json([
            'status' => true,
            'message' => 'Source Added Successfully',
        ]);
    }

    public function updateEpisodeSource(Request $request)
    {
        $episodeSource = EpisodeSource::where('episode_source_id', $request->episode_source_id)->first();
        if ($episodeSource == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        if ($episodeSource->type != $request->type && $episodeSource->type == Constants::FileType && $episodeSource->source != null) {
            GlobalFunction::deleteFile($episodeSource->source);
        }

        $episodeSource->title = $request->title;
        $episodeSource->quality = $request->quality;
        $episodeSource->size = $request->size;
        $episodeSource->is_download = $request->is_download;
        $episodeSource->type = $request->type;
        $episodeSource->access_type = $request->access_type;

        if ($request->type == Constants::FileType) {
            $episodeSource->source = $request->media;
        } elseif ($request->filled('source_url')) {
            $episodeSource->source = $request->source_url;
        }

        $episodeSource->save();

        return response()->json([
            'status' => true,
            'message' => 'Episode Source Updated Successfully',
        ]);
    }

    public function deleteEpisodeSource(Request $request)
    {
        $episodeSource = EpisodeSource::where('episode_source_id', $request->episode_source_id)->first();
        if ($episodeSource == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        if($episodeSource->type == Constants::FileType) {
            GlobalFunction::deleteFile($episodeSource->source);
        }
        $episodeSource->delete();

        return response()->json([
            'status' => true,
            'message' => 'Episode Source Delete Successfully',
        ]);
    }

    public function fetchEpisodeSubtitleList(Request $request)
    {
        $totalData = EpisodeSubtitle::where('episode_id', $request->episode_id)->count();
        $rows = EpisodeSubtitle::where('episode_id', $request->episode_id)->get();

        $result = $rows;

        $columns = array(
            0 => 'id'
        );

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');


        $totalFiltered = $totalData;
        if (empty($request->input('search.value'))) {
            $result = EpisodeSubtitle::offset($start)
                ->where('episode_id', $request->episode_id)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
        } else {
            $search = $request->input('search.value');
            $result =  EpisodeSubtitle::where('episode_id', $request->episode_id)
                ->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();

            $totalFiltered = $result->count();
        }

        $data = array();
        foreach ($result as $item) {

            $download = '<a download href="' . $item->file . '" class="me-2 btn btn-info px-3 text-white download shadow-none">' . __('download') . '</a>';

            $delete = '<a href="#" class="btn btn-danger px-3 text-white delete" rel=' . $item->episode_subtitle_id . ' >' . __('delete') . '</a>';

            $action = '<div class="text-end action"> '  . $download . $delete . ' </div>';

            $data[] = array(
                $item->language->title,
                $action,
            );
        }

        $json_data = array(
            "draw"            => intval($request->input('draw')),
            "recordsTotal"    => intval($totalData),
            "recordsFiltered" => $totalFiltered,
            "data"            => $data
        );
        echo json_encode($json_data);
        exit();
    }

    public function addEpisodeSubtitle(Request $request)
    {
        $subtitle = new EpisodeSubtitle();
        $subtitle->episode_id = $request->episode_id;
        $subtitle->language_id = $request->language_id;
       
        if ($request->hasFile('file')) {
            $file = $request->file('file');
            $filePath = GlobalFunction::saveSubtitleFileAsSrt($file);
            $subtitle->file = $filePath;
        }
        $subtitle->save();

        return response()->json([
            'status' => true,
            'message' => 'Subtitle Added Successfully',
        ]);
    }

    public function deleteEpisodeSubtitle(Request $request)
    {
        $episodeSubtitle = EpisodeSubtitle::where('episode_subtitle_id', $request->episode_subtitle_id)->first();
        if ($episodeSubtitle == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
 
        GlobalFunction::deleteFile($episodeSubtitle->file);

        $episodeSubtitle->delete();

        return response()->json([
            'status' => true,
            'message' => 'Episode Subtitle Delete Successfully',
        ]);
    }

    public function topContents()
    {
        $topContents = TopContent::get()->pluck('content_id');
        $contents = Content::whereNotIn('content_id', $topContents)->orderBy('created_at', 'DESC')->get();

        return view('topContents', [
            'contents' => $contents,
        ]);     
    }

    public function topContentsList(Request $request)
    {
        $sortBy = $request->input('sortBy', 'manual');
        $limit = $request->input('length', 20);
        $start = $request->input('start', 0);
        
        if ($sortBy === 'views') {
            // Get top 20 content by views
            $query = Content::where('is_show', 1)
                ->orderBy('total_view', 'DESC')
                ->orderBy('title', 'ASC');
                
            $totalData = Content::where('is_show', 1)->count();
            $result = $query->limit($limit)->offset($start)->get();
            
            $data = $result->map(function ($item, $index) use ($start) {
                $rank = $start + $index + 1;
                $imageUrl = $item->vertical_poster ? $item->vertical_poster : './assets/img/default.png';
                
                $image = "<img data-fancybox src='{$imageUrl}' class='object-cover img-fluid vertical_poster_tbl img-border border-radius'>";
                
                $type = $item->type == 1 ? '<span class="badge badge-primary">Movie</span>' : '<span class="badge badge-info">Series</span>';
                
                $details = "<a href='contentList/{$item->content_id}' class='btn btn-info me-2 shadow-none text-white'>" . __('details') . "</a>";
                
                return [
                    $rank,
                    $image,
                    $item->title,
                    $type,
                    number_format($item->total_view),
                    $item->ratings,
                    "<div class='text-end action'>{$details}</div>",
                ];
            });
            
        } elseif ($sortBy === 'ratings') {
            // Get top 20 content by ratings
            $query = Content::where('is_show', 1)
                ->orderBy('ratings', 'DESC')
                ->orderBy('total_view', 'DESC');
                
            $totalData = Content::where('is_show', 1)->count();
            $result = $query->limit($limit)->offset($start)->get();
            
            $data = $result->map(function ($item, $index) use ($start) {
                $rank = $start + $index + 1;
                $imageUrl = $item->vertical_poster ? $item->vertical_poster : './assets/img/default.png';
                
                $image = "<img data-fancybox src='{$imageUrl}' class='object-cover img-fluid vertical_poster_tbl img-border border-radius'>";
                
                $type = $item->type == 1 ? '<span class="badge badge-primary">Movie</span>' : '<span class="badge badge-info">Series</span>';
                
                $details = "<a href='contentList/{$item->content_id}' class='btn btn-info me-2 shadow-none text-white'>" . __('details') . "</a>";
                
                return [
                    $rank,
                    $image,
                    $item->title,
                    $type,
                    number_format($item->total_view),
                    $item->ratings,
                    "<div class='text-end action'>{$details}</div>",
                ];
            });
            
        } else {
            // Manual selection mode
            $query = TopContent::with('content')->orderBy('content_index', 'ASC');
            $totalData = $query->count();
            $result = $query->get();
            
            $data = $result->map(function ($item) {
                if (!$item->content) {
                    return null;
                }
                
                $imageUrl = $item->content->vertical_poster ? $item->content->vertical_poster : './assets/img/default.png';
                
                $image = "<img data-fancybox src='{$imageUrl}' class='object-cover img-fluid vertical_poster_tbl img-border border-radius'>";
                
                $type = $item->content->type == 1 ? '<span class="badge badge-primary">Movie</span>' : '<span class="badge badge-info">Series</span>';
                
                $remove = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->top_content_id}'>" . __('delete') . "</a>";
                
                return [
                    $item->content_index,
                    $image,
                    $item->content->title,
                    $type,
                    number_format($item->content->total_view),
                    $item->content->ratings,
                    "<div class='text-end action'>{$remove}</div>",
                    'DT_RowId' => $item->top_content_id
                ];
            })->filter();
        }

        $json_data = [
            "draw" => intval($request->input('draw', 1)),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalData),
            "data" => $data->values(),
        ];

        return response()->json($json_data);
    }

    public function saveOrder(Request $request)
    {
        $order = $request->order;

        foreach ($order as $index => $id) {
            TopContent::where('top_content_id', $id)->update(['content_index' => $index + 1]);
        }

        return response()->json(['success' => true]);
    }

    public function removeFromTopContent(Request $request)
    {
        $topContent = TopContent::where('top_content_id', $request->top_content_id)->first();

        if ($topContent == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $removedIndex = $topContent->content_index;

        $topContent->delete();

        $remainingContents = TopContent::where('content_index', '>', $removedIndex)->get();
        foreach ($remainingContents as $content) {
            $content->content_index -= 1;
            $content->save();
        }


        return response()->json([
            'status' => true,
            'message' => 'Top Content Removed Successfully',
            'data' => $topContent,
        ]);
    }

    public function addToTopContent(Request $request)
    {
        
        $topContents = TopContent::get();
        if ($topContents == null) {
            $topContent = new TopContent();
            $topContent->content_id = $request->content_id;
            $topContent->content_index = 1;
            $topContent->save();
            return response()->json([
                'status' => true,
                'message' => 'Top Content Added Successfully',
                'data' => $topContent,
            ]);          
        }  
        
        else
        {
            $topContent = new TopContent();
            $topContent->content_id = $request->content_id;
            $topContent->content_index = $topContents->count() + 1;
            $topContent->save();
            return response()->json([
                'status' => true,
                'message' => 'Top Content Added Successfully',
                'data' => $topContent,
            ]);
        }
    }

    public function notifyContent(Request $request)
    {
        $title = $request->title;
        $description = $request->description;
        $content_id = $request->content_id;

        GlobalFunction::sendPushNotificationToAllUsers($title, $description, $content_id);

        return response()->json([
            'status' => true,
            'message' => 'Notification Send Successfully.',
        ]);
    }

    // Audio Track Management
    public function fetchAudioTrackList(Request $request)
    {
        $contentId = $request->input('content_id');
        $contentSourceId = $request->input('content_source_id');
        
        $query = \App\Models\ContentAudioTrack::with('language')
            ->where('content_id', $contentId);
        
        if ($contentSourceId) {
            $query->where('content_source_id', $contentSourceId);
        }
        
        $audioTracks = $query->orderBy('sort_order')->get();
        
        $data = $audioTracks->map(function ($track) {
            $title = '<span>' . $track->title . '</span>';
            $language = '<span>' . ($track->language ? $track->language->language_name : $track->language_code) . '</span>';
            $format = '<span>' . $track->audio_format . ' / ' . $track->audio_channels . '</span>';
            $default = $track->is_default ? '<span class="badge bg-success">Default</span>' : '';
            
            $edit = '<a rel="' . $track->id . '" 
                    data-title="' . $track->title . '"
                    data-language_id="' . $track->language_id . '"
                    data-language_code="' . $track->language_code . '"
                    data-audio_url="' . $track->audio_url . '"
                    data-audio_format="' . $track->audio_format . '"
                    data-audio_channels="' . $track->audio_channels . '"
                    data-is_default="' . $track->is_default . '"
                    data-sort_order="' . $track->sort_order . '"
                    class="me-2 btn btn-success px-3 text-white editAudioTrack">' . __('edit') . '</a>';
            $delete = '<a href="#" class="btn btn-danger px-3 text-white deleteAudioTrack" rel="' . $track->id . '">' . __('delete') . '</a>';
            $action = '<div class="text-end">' . $edit . $delete . '</div>';
            
            return [
                $title,
                $language,
                $format,
                $default,
                $action
            ];
        });
        
        return response()->json([
            "draw" => intval($request->input('draw')),
            "recordsTotal" => count($data),
            "recordsFiltered" => count($data),
            "data" => $data
        ]);
    }

    public function addAudioTrack(Request $request)
    {
        $audioTrack = new \App\Models\ContentAudioTrack();
        $audioTrack->content_id = $request->content_id;
        $audioTrack->content_source_id = $request->content_source_id;
        $audioTrack->language_id = $request->language_id;
        $audioTrack->title = $request->title;
        $audioTrack->language_code = $request->language_code;
        $audioTrack->audio_url = $request->audio_url;
        $audioTrack->audio_format = $request->audio_format ?? 'AAC';
        $audioTrack->audio_channels = $request->audio_channels ?? 'Stereo';
        $audioTrack->is_default = $request->is_default ?? 0;
        $audioTrack->sort_order = $request->sort_order ?? 0;
        $audioTrack->save();
        
        return response()->json([
            'status' => true,
            'message' => 'Audio track added successfully'
        ]);
    }

    public function updateAudioTrack(Request $request)
    {
        $audioTrack = \App\Models\ContentAudioTrack::find($request->id);
        if (!$audioTrack) {
            return response()->json([
                'status' => false,
                'message' => 'Audio track not found'
            ]);
        }
        
        $audioTrack->language_id = $request->language_id;
        $audioTrack->title = $request->title;
        $audioTrack->language_code = $request->language_code;
        $audioTrack->audio_url = $request->audio_url;
        $audioTrack->audio_format = $request->audio_format ?? 'AAC';
        $audioTrack->audio_channels = $request->audio_channels ?? 'Stereo';
        $audioTrack->is_default = $request->is_default ?? 0;
        $audioTrack->sort_order = $request->sort_order ?? 0;
        $audioTrack->save();
        
        return response()->json([
            'status' => true,
            'message' => 'Audio track updated successfully'
        ]);
    }

    public function deleteAudioTrack(Request $request)
    {
        $audioTrack = \App\Models\ContentAudioTrack::find($request->id);
        if (!$audioTrack) {
            return response()->json([
                'status' => false,
                'message' => 'Audio track not found'
            ]);
        }
        
        $audioTrack->delete();
        
        return response()->json([
            'status' => true,
            'message' => 'Audio track deleted successfully'
        ]);
    }

    // Subtitle Track Management
    public function fetchSubtitleTrackList(Request $request)
    {
        $contentId = $request->input('content_id');
        $contentSourceId = $request->input('content_source_id');
        
        $query = \App\Models\ContentSubtitleTrack::with('language')
            ->where('content_id', $contentId);
        
        if ($contentSourceId) {
            $query->where('content_source_id', $contentSourceId);
        }
        
        $subtitleTracks = $query->orderBy('sort_order')->get();
        
        $data = $subtitleTracks->map(function ($track) {
            $title = '<span>' . $track->title . '</span>';
            $language = '<span>' . ($track->language ? $track->language->language_name : $track->language_code) . '</span>';
            $format = '<span>' . $track->subtitle_format . '</span>';
            $type = '<span>' . $track->subtitle_type . '</span>';
            $badges = '';
            if ($track->is_default) $badges .= '<span class="badge bg-success me-1">Default</span>';
            if ($track->is_sdh) $badges .= '<span class="badge bg-info me-1">SDH</span>';
            if ($track->is_forced) $badges .= '<span class="badge bg-warning">Forced</span>';
            
            $edit = '<a rel="' . $track->id . '" 
                    data-title="' . $track->title . '"
                    data-language_id="' . $track->language_id . '"
                    data-language_code="' . $track->language_code . '"
                    data-subtitle_url="' . $track->subtitle_url . '"
                    data-subtitle_format="' . $track->subtitle_format . '"
                    data-subtitle_type="' . $track->subtitle_type . '"
                    data-is_default="' . $track->is_default . '"
                    data-is_sdh="' . $track->is_sdh . '"
                    data-is_forced="' . $track->is_forced . '"
                    data-sort_order="' . $track->sort_order . '"
                    class="me-2 btn btn-success px-3 text-white editSubtitleTrack">' . __('edit') . '</a>';
            $delete = '<a href="#" class="btn btn-danger px-3 text-white deleteSubtitleTrack" rel="' . $track->id . '">' . __('delete') . '</a>';
            $action = '<div class="text-end">' . $edit . $delete . '</div>';
            
            return [
                $title,
                $language,
                $format,
                $type,
                $badges,
                $action
            ];
        });
        
        return response()->json([
            "draw" => intval($request->input('draw')),
            "recordsTotal" => count($data),
            "recordsFiltered" => count($data),
            "data" => $data
        ]);
    }

    public function addSubtitleTrack(Request $request)
    {
        $subtitleTrack = new \App\Models\ContentSubtitleTrack();
        $subtitleTrack->content_id = $request->content_id;
        $subtitleTrack->content_source_id = $request->content_source_id;
        $subtitleTrack->language_id = $request->language_id;
        $subtitleTrack->title = $request->title;
        $subtitleTrack->language_code = $request->language_code;
        $subtitleTrack->subtitle_url = $request->subtitle_url;
        $subtitleTrack->subtitle_format = $request->subtitle_format ?? 'SRT';
        $subtitleTrack->subtitle_type = $request->subtitle_type ?? 'dialogue';
        $subtitleTrack->is_default = $request->is_default ?? 0;
        $subtitleTrack->is_sdh = $request->is_sdh ?? 0;
        $subtitleTrack->is_forced = $request->is_forced ?? 0;
        $subtitleTrack->sort_order = $request->sort_order ?? 0;
        $subtitleTrack->save();
        
        return response()->json([
            'status' => true,
            'message' => 'Subtitle track added successfully'
        ]);
    }

    public function updateSubtitleTrack(Request $request)
    {
        $subtitleTrack = \App\Models\ContentSubtitleTrack::find($request->id);
        if (!$subtitleTrack) {
            return response()->json([
                'status' => false,
                'message' => 'Subtitle track not found'
            ]);
        }
        
        $subtitleTrack->language_id = $request->language_id;
        $subtitleTrack->title = $request->title;
        $subtitleTrack->language_code = $request->language_code;
        $subtitleTrack->subtitle_url = $request->subtitle_url;
        $subtitleTrack->subtitle_format = $request->subtitle_format ?? 'SRT';
        $subtitleTrack->subtitle_type = $request->subtitle_type ?? 'dialogue';
        $subtitleTrack->is_default = $request->is_default ?? 0;
        $subtitleTrack->is_sdh = $request->is_sdh ?? 0;
        $subtitleTrack->is_forced = $request->is_forced ?? 0;
        $subtitleTrack->sort_order = $request->sort_order ?? 0;
        $subtitleTrack->save();
        
        return response()->json([
            'status' => true,
            'message' => 'Subtitle track updated successfully'
        ]);
    }

    public function deleteSubtitleTrack(Request $request)
    {
        $subtitleTrack = \App\Models\ContentSubtitleTrack::find($request->id);
        if (!$subtitleTrack) {
            return response()->json([
                'status' => false,
                'message' => 'Subtitle track not found'
            ]);
        }
        
        $subtitleTrack->delete();
        
        return response()->json([
            'status' => true,
            'message' => 'Subtitle track deleted successfully'
        ]);
    }

}
