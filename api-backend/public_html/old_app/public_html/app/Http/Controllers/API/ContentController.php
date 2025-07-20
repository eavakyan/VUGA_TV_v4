<?php

namespace App\Http\Controllers\API;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rule;
use Validator;
use Hash;
use DB;
use File;
use Log;
use App\Admin;
use App\Content;
use App\User;
use App\Genre;
use App\Language;
use App\ContentSource;
use App\MovieCast;
use App\Actor;
use App\ContentSubtitles;
use App\SeriesSeason;
use App\SeasonEpisode;
use App\EpisodeSource;
use App\EpisodeSubtitles;
use App\Common;
use App\Watchlist;
use App\Comment;
use Laravel\Passport\Token;

class ContentController extends Controller
{

    public function GetHomeContentList(Request $request)
    {


        // $user_id = $request->user()->user_id;

        // if (empty($user_id)) {
        //     $msg = "user id is required";
        //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
        // }

        $headers = $request->headers->all();

        $verify_request_base = Admin::verify_request_base($headers);

        if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
            return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
            exit();
        }

        $rules = [
            'user_id' => 'required',
        ];

        $validator = Validator::make($request->all(), $rules);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => 401, 'message' => $msg]);
        }
        $user_id = $request->get('user_id');

        $User =  User::where('user_id', $user_id)->first();
        if (empty($User)) {
            return response()->json(['status' => 401, 'message' => "User Not Found"]);
        }
        // $limit = $request->get('limit') ? $request->get('limit') : 20;
        // $start = $request->get('start') ? $request->get('start') : 0;
        // $user_id = "";
        // if(auth()->guard('api')->user()){
        //     $user_id = auth()->guard('api')->user()->user_id;
        // }
        $ContentData =  Content::select('tbl_content.id', 'tbl_content.content_id', 'tbl_content.content_type', 'tbl_content.content_title', 'tbl_content.verticle_poster', 'tbl_content.horizontal_poster', 'tbl_content.release_year', 'tbl_content.ratings', DB::raw("GROUP_CONCAT(g.genre_name SEPARATOR ', ') as genre_name"))->leftjoin("tbl_genre as g", \DB::raw("FIND_IN_SET(g.genre_id,tbl_content.genre_id)"), ">", DB::raw("'0'"))->where('tbl_content.is_featured', 1)->groupBy('tbl_content.id')->orderBy('tbl_content.id', 'ASC')->offset(0)->limit(10)->get();

        $watchlistData =  Watchlist::select('C.id', 'C.content_id', 'C.content_type', 'C.content_title', 'C.verticle_poster', 'C.horizontal_poster', 'C.release_year', 'C.ratings', DB::raw("GROUP_CONCAT(g.genre_name SEPARATOR ', ') as genre_name"))->join("tbl_content as C", 'C.content_id', 'tbl_watchlist.content_id')->leftjoin("tbl_genre as g", \DB::raw("FIND_IN_SET(g.genre_id,C.genre_id)"), ">", DB::raw("'0'"))->where('tbl_watchlist.user_id', $user_id)->groupBy('C.id')->orderBy('C.id', 'ASC')->offset(0)->limit(10)->get();

        $GenreData =  Genre::orderBy('genre_id', 'DESC')
            // ->offset(0)
            // ->limit(10)
            ->get();
            
        $Contentlist = null;
        if (count($GenreData) > 0) {
            $Contentlist = Common::GetContentDataByGenre($GenreData, $user_id);
            $Contentlist = array_values($Contentlist);
        }

        return response()->json(['status' => 200, 'message' => "Content Data Get Successfully.", 'featured' => $ContentData, 'watchlist' => $watchlistData, 'data' => $Contentlist]);
    }

    public function getAllContentList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;


            $ContentData =  Content::select('content_id', 'content_type', 'content_title', 'ratings', 'verticle_poster', 'horizontal_poster')->orderBy('id', 'ASC')->offset($start)->limit($limit)->get();

            // $ContentData =  Content::select('content_id',DB::raw('(CASE WHEN content_type = 1 THEN "Movie" ELSE "Series" END) AS content_type'),'content_title','verticle_poster','horizontal_poster')->orderBy('id','DESC')->offset($start)->limit($limit)->get();

            if (count($ContentData) > 0) {
                // $popularData = Common::GetContentData($ContentData,$user_id,1);
                return response()->json(['status' => 200, 'message' => "All Content List Get Successfully.", 'data' => $ContentData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function searchContent(Request $request)
    {
      
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $search = $request->get('search_keyword');
            $content_type = $request->get('content_type');
            $genre_id = $request->get('genre_id');
            $language_id = $request->get('language_id');
            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;

            if ($search) {
                $query =  Content::select('content_id', 'content_type', 'content_title', 'ratings', 'verticle_poster', 'horizontal_poster');
                if ($content_type) {
                    $query->where('content_type', $content_type);
                }
                if ($language_id) {
                    $query->where('language_id', $language_id);
                }
                if ($genre_id) {
                    $query->whereRaw("FIND_IN_SET( " . $genre_id . " , genre_id) ");
                }
                // ->orWhere('description', 'LIKE',"%{$search}%")
                $ContentData =  $query->where('content_title', 'LIKE', "%{$search}%")->offset($start)
                    ->limit($limit)->orderBy('id', 'ASC')->offset($start)->limit($limit)->get();
            } else {
                $query =  Content::select('content_id', 'content_type', 'content_title', 'ratings', 'verticle_poster', 'horizontal_poster');
                if ($content_type) {
                    $query->where('content_type', $content_type);
                }
                if ($language_id) {
                    $query->where('language_id', $language_id);
                }
                if ($genre_id) {
                    $query->whereRaw("FIND_IN_SET( " . $genre_id . " , genre_id) ");
                }
                $ContentData =  $query->orderBy('id', 'ASC')->offset($start)->limit($limit)->get();
            }

            if (count($ContentData) > 0) {
                // $popularData = Common::GetContentData($ContentData,$user_id,1);
                return response()->json(['status' => 200, 'message' => "Content List Get Successfully.", 'data' => $ContentData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function getMovieList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;


            $ContentData =  Content::select('content_id', 'content_type', 'content_title', 'ratings', 'verticle_poster', 'horizontal_poster')->where('content_type', 1)->orderBy('id', 'ASC')->offset($start)->limit($limit)->get();

            // $ContentData =  Content::select('content_id',DB::raw('(CASE WHEN content_type = 1 THEN "Movie" ELSE "Series" END) AS content_type'),'content_title','verticle_poster','horizontal_poster')->orderBy('id','DESC')->offset($start)->limit($limit)->get();

            if (count($ContentData) > 0) {
                return response()->json(['status' => 200, 'message' => "Movie List Get Successfully.", 'data' => $ContentData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }


    public function getSeriesList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;


            $ContentData =  Content::select('content_id', 'content_type', 'content_title', 'ratings', 'verticle_poster', 'horizontal_poster')->where('content_type', 2)->orderBy('id', 'ASC')->offset($start)->limit($limit)->get();

            // $ContentData =  Content::select('content_id',DB::raw('(CASE WHEN content_type = 1 THEN "Movie" ELSE "Series" END) AS content_type'),'content_title','verticle_poster','horizontal_poster')->orderBy('id','DESC')->offset($start)->limit($limit)->get();

            if (count($ContentData) > 0) {
                return response()->json(['status' => 200, 'message' => "Series List Get Successfully.", 'data' => $ContentData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function getAllLanguageList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            // $rules = [
            //     'start' => 'required',
            // ];

            // $validator = Validator::make($request->all(), $rules);

            // if ($validator->fails()) {
            //     $messages = $validator->errors()->all();
            //     $msg = $messages[0];
            //     return response()->json(['status' => 401, 'message' => $msg]);
            // }

            // $limit = $request->get('limit') ? $request->get('limit') : 20;
            // $start = $request->get('start') ? $request->get('start') : 0;


            $LanguageData =  Language::select('language_id', 'language_name')->get();

            if (count($LanguageData) > 0) {
                return response()->json(['status' => 200, 'message' => "Language List Get Successfully.", 'data' => $LanguageData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function getAllGenreList(Request $request)
    {
      
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;


            $GenreData =  Genre::select('genre_id', 'genre_name')->offset($start)->limit($limit)->get();

            if (count($GenreData) > 0) {
                return response()->json(['status' => 200, 'message' => "Genre List Get Successfully.", 'data' => $GenreData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function getContentListByGenreID(Request $request)
    {
      

            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }

            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'genre_id' => 'required',
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }
            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;
            $genre_id = $request->get('genre_id');

            $ContentData =  Content::select('*')
                ->whereRaw("FIND_IN_SET( " . $genre_id . " , genre_id) ")
                ->orderBy('id', 'DESC')
                ->offset($start)
                ->limit($limit)
                ->get();
            $Contentlist = [];
            if ($ContentData) {
                $i = 0;
                foreach ($ContentData as $key => $value) {
                    $Contentlist[$i]['id'] = $value['id'];
                    $Contentlist[$i]['content_id'] = $value['content_id'];
                    $Contentlist[$i]['content_type'] = $value['content_type'];
                    $Contentlist[$i]['content_title'] = $value['content_title'];
                    $Contentlist[$i]['verticle_poster'] = $value['verticle_poster'];
                    $Contentlist[$i]['horizontal_poster'] = $value['horizontal_poster'];
                    $i++;
                }
            }

            return response()->json(['status' => 200, 'message' => "Content Data Get Successfully.", 'data' => $Contentlist]);
       
    }

    public function getContentDetailsByID(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'user_id' => 'required',
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $user_id = $request->get('user_id');

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $content_id = $request->get('content_id');


            $ContentData =  Content::select('tbl_content.id', 'tbl_content.content_id', 'tbl_content.content_type', 'tbl_content.content_title', 'tbl_content.description', 'tbl_content.duration', 'tbl_content.release_year', 'tbl_content.ratings', 'tbl_content.download_link', 'tbl_content.trailer_url', 'tbl_content.verticle_poster', 'tbl_content.horizontal_poster', 'tbl_content.total_view', 'tbl_content.total_download', 'tbl_content.total_share', 'l.language_name', 'tbl_content.genre_id', DB::raw("GROUP_CONCAT(g.genre_name SEPARATOR ',') as genre_name"))->leftjoin("tbl_genre as g", \DB::raw("FIND_IN_SET(g.genre_id,tbl_content.genre_id)"), ">", DB::raw("'0'"))->leftjoin("tbl_language as l", 'l.language_id', 'tbl_content.language_id')->where('tbl_content.content_id', $content_id)->groupBy('tbl_content.id')->orderBy('tbl_content.id', 'ASC')->first();

            $watchlistData =  Watchlist::where('user_id', $user_id)->where('content_id', $content_id)->first();
            if ($watchlistData) {
                $ContentData['is_addedtoWatchlist'] = 1;
            } else {
                $ContentData['is_addedtoWatchlist'] = 0;
            }

            $sourceData = ContentSource::select('source_title', 'source_quality', 'source_size', 'downloadable', 'access_type', 'source_type', 'source')->where('content_id', $content_id)->get();
            if ($sourceData) {
                $ContentData['source'] = $sourceData;
            } else {
                $ContentData['source'] = [];
            }

            $subtitlesData = ContentSubtitles::select('tbl_content_subtitles.subtitle_file', 'l.language_name')->leftjoin("tbl_language as l", 'l.language_id', 'tbl_content_subtitles.language_id')->where('tbl_content_subtitles.content_id', $content_id)->get();
            if ($subtitlesData) {
                $ContentData['subtitles'] = $subtitlesData;
            } else {
                $ContentData['subtitles'] = [];
            }

            if ($ContentData['content_type'] == 2) {
                $SeasonData = SeriesSeason::select('season_id', 'season_title', 'trailer_url')->where('content_id', $content_id)->with('episodes')->with('episodes.sources')->get();
                foreach ($SeasonData as $skey => $sval) {
                    foreach ($sval['episodes'] as $sekey => $seval) {
                        $esubtitlesData = EpisodeSubtitles::select('tbl_episode_subtitles.subtitle_file', 'l.language_name')->leftjoin("tbl_language as l", 'l.language_id', 'tbl_episode_subtitles.language_id')->where('tbl_episode_subtitles.episode_id', $seval['episode_id'])->get();
                        $SeasonData[$skey]['episodes'][$sekey]['subtitles'] = $esubtitlesData;
                    }
                }
                if ($SeasonData) {
                    $ContentData['season'] = $SeasonData;
                } else {
                    $ContentData['season'] = [];
                }
            } else {
                $ContentData['season'] = [];
            }

            if ($ContentData['content_type'] == 1) {
                $castData = MovieCast::select('a.actor_name', 'a.actor_image', 'tbl_movie_cast.charactor_name')->leftjoin("tbl_actor as a", 'a.actor_id', 'tbl_movie_cast.actor_id')->where('tbl_movie_cast.content_id', $content_id)->get();
                $ContentData['cast'] = $castData;
            }

            $GenreData = explode(',', $ContentData['genre_id']);
            foreach ($GenreData as $gkey => $gvalue) {
                $MoreContentData =  Content::select('*')
                    ->whereRaw("FIND_IN_SET( " . $gvalue . " , genre_id) ")
                    ->orderBy('id', 'DESC')
                    ->offset(0)
                    ->limit(5)
                    ->get();
                if ($MoreContentData) {
                    $i = 0;

                    foreach ($MoreContentData as $key => $value) {
                        $list[$i]['id'] = $value['id'];
                        $list[$i]['content_id'] = $value['content_id'];
                        $list[$i]['content_type'] = $value['content_type'];
                        $list[$i]['content_title'] = $value['content_title'];
                        $list[$i]['verticle_poster'] = $value['verticle_poster'];
                        $list[$i]['horizontal_poster'] = $value['horizontal_poster'];
                        $i++;
                    }
                    $list = array_map("unserialize", array_unique(array_map("serialize", $list)));

                    $ContentData['more_like_this'] = array_values($list);
                } else {
                    $ContentData['more_like_this'] = [];
                }
            }
            $ContentData['genre_id'] =  $GenreData;
            $ContentData['genre_name'] =  explode(',', $ContentData['genre_name']);

            $commentData = Comment::select('tbl_comment.*', 'u.fullname', 'u.profile_image')->leftjoin("tbl_users as u", 'u.user_id', 'tbl_comment.user_id')->where('tbl_comment.content_id', $content_id)->where('tbl_comment.status', 1)->get();
            $ContentData['comments'] =  $commentData;
            if ($ContentData) {
                return response()->json(['status' => 200, 'message' => "Content Data Get Successfully.", 'data' => $ContentData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function increaseContentView(Request $request)
    {
      
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $content_id = $request->get('content_id');

            $ContentData =  Content::where('content_id', $content_id)->increment('total_view', 1);

            return response()->json(['status' => 200, 'message' => "Content View Successfully."]);
       
    }

    public function increaseContentDownload(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $content_id = $request->get('content_id');

            $ContentData =  Content::where('content_id', $content_id)->increment('total_download', 1);

            return response()->json(['status' => 200, 'message' => "Content Download Successfully."]);
       
    }

    public function increaseContentShare(Request $request)
    {
    
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $content_id = $request->get('content_id');

            $ContentData =  Content::where('content_id', $content_id)->increment('total_share', 1);

            return response()->json(['status' => 200, 'message' => "Content Share Successfully."]);
        
    }

    public function addComment(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'user_id' => 'required',
                'content_id' => 'required',
                'comment' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $user_id = $request->get('user_id');

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $data['user_id'] = $user_id;
            $data['content_id'] = $request->get('content_id');
            $data['comment'] = $request->get('comment');

            $ContentData =  Comment::insert($data);;

            return response()->json(['status' => 200, 'message' => "Comment Added Successfully."]);
    
    }

    public function getSourceByContentID(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $content_id = $request->get('content_id');
            $checkContent = Content::where('content_id', $content_id)->first();

            if (empty($checkContent)) {
                return response()->json(['status' => 401, 'message' => "Content is Not Exist."]);
            }

            $sourceData = ContentSource::select('source_title', 'source_quality', 'source_size', 'downloadable', 'access_type', 'source_type')->where('content_id', $content_id)->get();

            if ($sourceData) {
                return response()->json(['status' => 200, 'message' => "Source Data Get Successfully.", 'data' => $sourceData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function getSubtitlesByContentID(Request $request)
    {
    
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $content_id = $request->get('content_id');
            $checkContent = Content::where('content_id', $content_id)->first();

            if (empty($checkContent)) {
                return response()->json(['status' => 401, 'message' => "Content is Not Exist."]);
            }

            $subtitlesData = ContentSubtitles::select('subtitles_id', 'language_id', 'subtitle_file')->where('content_id', $content_id)->get();

            if ($subtitlesData) {
                return response()->json(['status' => 200, 'message' => "Subtitles Data Get Successfully.", 'data' => $subtitlesData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
        
    }

    public function getSeasonByContentID(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $content_id = $request->get('content_id');
            $checkContent = Content::where('content_id', $content_id)->first();

            if (empty($checkContent)) {
                return response()->json(['status' => 401, 'message' => "Content is Not Exist."]);
            }
            if (!empty($checkContent) && $checkContent['content_type'] != 2) {
                return response()->json(['status' => 401, 'message' => "Wrong Content Id. Series Content Id is Required."]);
            }
            $seasonData = SeriesSeason::select('season_id', 'season_title', 'trailer_url')->where('content_id', $content_id)->get();

            if ($seasonData) {
                return response()->json(['status' => 200, 'message' => "Season Data Get Successfully.", 'data' => $seasonData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
        
    }

    public function getEpisodeBySeasonID(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'season_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $season_id = $request->get('season_id');

            $episodeData = SeasonEpisode::select('season_id', 'episode_id', 'episode_title', 'episode_thumb', 'episode_description', 'episode_duration', 'access_type')->where('season_id', $season_id)->with('sources')->with('subtitles')->get();

            if ($episodeData) {
                return response()->json(['status' => 200, 'message' => "Episode Data Get Successfully.", 'data' => $episodeData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
    
    }

    public function getSourceByEpisodeID(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'episode_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $episode_id = $request->get('episode_id');

            $sourceData = EpisodeSource::select('source_title', 'source_quality', 'source_size', 'downloadable', 'access_type', 'source_type')->where('episode_id', $episode_id)->get();

            if ($sourceData) {
                return response()->json(['status' => 200, 'message' => "Source Data Get Successfully.", 'data' => $sourceData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function getSubtitlesByEpisodeID(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'episode_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $episode_id = $request->get('episode_id');

            $subtitlesData = EpisodeSubtitles::select('subtitles_id', 'language_id', 'subtitle_file')->where('episode_id', $episode_id)->get();

            if ($subtitlesData) {
                return response()->json(['status' => 200, 'message' => "Subtitles Data Get Successfully.", 'data' => $subtitlesData]);
            } else {
                return response()->json(['status' => 401, 'message' => "No Data Found."]);
            }
       
    }

    public function addToWatchList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'user_id' => 'required',
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }
            $user_id = $request->get('user_id');

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }
            $content_id = $request->get('content_id');

            $watchlistData = Watchlist::where('user_id', $user_id)->where('content_id', $content_id)->first();

            if (empty($watchlistData)) {

                $data['user_id'] = $user_id;
                $data['content_id'] = $content_id;

                Watchlist::insert($data);

                return response()->json(['status' => 200, 'message' => "Content Added to Watchlist Successfully."]);
            } else {
                return response()->json(['status' => 401, 'message' => "Content Already into Watchlist."]);
            }
       
    }

    public function removeFromWatchList(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'user_id' => 'required',
                'content_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $user_id = $request->get('user_id');

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $content_id = $request->get('content_id');

            $watchlistData = Watchlist::where('user_id', $user_id)->where('content_id', $content_id)->first();

            if (empty($watchlistData)) {

                return response()->json(['status' => 401, 'message' => "Content is not in Watchlist."]);
            } else {

                Watchlist::where('user_id', $user_id)->where('content_id', $content_id)->delete();

                return response()->json(['status' => 200, 'message' => "Content Removed From Watchlist Successfully."]);
            }
       
    }

    public function getWatchlist(Request $request)
    {
       

            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }

            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'user_id' => 'required',
                'start' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $user_id = $request->get('user_id');

            $User =  User::where('user_id', $user_id)->first();
            if (empty($User)) {
                return response()->json(['status' => 401, 'message' => "User Not Found"]);
            }

            $limit = $request->get('limit') ? $request->get('limit') : 20;
            $start = $request->get('start') ? $request->get('start') : 0;

            $watchlistData =  Watchlist::select('C.id', 'C.content_id', 'C.content_type', 'C.content_title', 'C.verticle_poster', 'C.horizontal_poster', 'C.release_year', 'C.ratings', DB::raw("GROUP_CONCAT(g.genre_name SEPARATOR ', ') as genre_name"))->join("tbl_content as C", 'C.content_id', 'tbl_watchlist.content_id')->join("tbl_genre as g", \DB::raw("FIND_IN_SET(g.genre_id,C.genre_id)"), ">", DB::raw("'0'"))->where('tbl_watchlist.user_id', $user_id)->groupBy('C.id')->orderBy('C.id', 'ASC')->offset($start)->limit($limit)->get();

            return response()->json(['status' => 200, 'message' => "Watchlist Data Get Successfully.", 'data' => $watchlistData]);
       
    }

    public function increaseEpisodeView(Request $request)
    {
       
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'episode_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $episode_id = $request->get('episode_id');

            SeasonEpisode::where('episode_id', $episode_id)->increment('total_view', 1);

            return response()->json(['status' => 200, 'message' => "Episode View Successfully."]);
        
    }

    public function increaseEpisodeDownload(Request $request)
    {
      
            // $user_id = $request->user()->user_id;

            // if (empty($user_id)) {
            //     $msg = "user id is required";
            //     return response()->json(['success_code' => 401, 'response_code' => 0, 'response_message' => $msg]);
            // }


            $headers = $request->headers->all();

            $verify_request_base = Admin::verify_request_base($headers);

            if (isset($verify_request_base['status']) && $verify_request_base['status'] == 401) {
                return response()->json(['success_code' => 401, 'message' => "Unauthorized Access!"]);
                exit();
            }

            $rules = [
                'episode_id' => 'required',
            ];

            $validator = Validator::make($request->all(), $rules);

            if ($validator->fails()) {
                $messages = $validator->errors()->all();
                $msg = $messages[0];
                return response()->json(['status' => 401, 'message' => $msg]);
            }

            $episode_id = $request->get('episode_id');

            SeasonEpisode::where('episode_id', $episode_id)->increment('total_download', 1);

            return response()->json(['status' => 200, 'message' => "Episode Download Successfully."]);
      
    }
}
