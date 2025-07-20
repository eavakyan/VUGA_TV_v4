<?php

namespace App\Http\Controllers\admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use Session;
use File;
use DB;
use App\Common;
use App\Content;
use App\User;
use App\Genre;
use App\Language;
use App\Watchlist;
use App\ContentSource;
use App\MovieCast;
use App\Actor;
use App\ContentSubtitles;
use App\SeriesSeason;
use App\SeasonEpisode;
use App\EpisodeSource;
use App\EpisodeSubtitles;
use App\Comment;
use App\GlobalFunction;
use App\Notification;
use App\Settings;

class ContentController extends Controller
{

	public function viewListContent()
	{
		$total_content = Content::count();
		$total_movie = Content::where('content_type', 1)->count();
		$total_series = Content::where('content_type', 2)->count();
		return view('admin.content.content_list')->with('total_content', $total_content)->with('total_movie', $total_movie)->with('total_series', $total_series);
	}

	public function viewAddContent()
	{
		$genredata = Genre::get();
		$languagedata = Language::get();
		return view('admin.content.content_addupdate')->with('genredata', $genredata)->with('languagedata', $languagedata)->with('sourceData', [])->with('data', [])->with('content_title', "")->with('content_id', 0)->with('content_type', 1)->with('title', 'Add');
	}

	public function viewUpdateContent($flag, $id = "")
	{
		$data = Content::where('content_id', $id)->first();
		$genredata = Genre::get();
		$languagedata = Language::get();
		if ($data['total_view']) {
			$data['total_view'] = Common::number_format_short($data['total_view']);
		}
		if ($data['total_download']) {
			$data['total_download'] = Common::number_format_short($data['total_download']);
		}
		if ($data['total_share']) {
			$data['total_share'] = Common::number_format_short($data['total_share']);
		}

		return view('admin.content.content_addupdate')->with('genredata', $genredata)->with('languagedata', $languagedata)->with('data', $data)->with('content_id', $id)->with('content_title', $data['content_title'])->with('content_type', $flag)->with('title', 'Edit');
	}

	public function addUpdateContent(Request $request)
	{
		$content_title = $request->input('content_title');
		$description = $request->input('description');
		$content_type = $request->input('content_type');
		$duration = $request->input('duration');
		$release_year = $request->input('release_year');
		$ratings = $request->input('ratings');
		$language_id = $request->input('language_id');
		$genre_id = $request->input('genre_id');
		$download_link = $request->input('download_link');
		$trailer_url = $request->input('trailer_url');
		$content_id = $request->input('content_id');
		$action = $request->input('action');
		$is_notify = $request->input('is_notify');

		if (empty($content_id)) {
			$content_id = Content::get_random_string();
		}

		$hidden_verticle_poster = $request->input('hidden_verticle_poster');
		$hidden_horizontal_poster = $request->input('hidden_horizontal_poster');

		$verticle_poster = $request->file('verticle_poster');

		if ($request->hasfile('verticle_poster')) {
			$verticle_poster = GlobalFunction::saveFileAndGivePath($request->file('verticle_poster'));
		} else {
			$verticle_poster = $hidden_verticle_poster;
		}
		$data['verticle_poster'] = $verticle_poster;

		if ($request->hasfile('horizontal_poster')) {
			$horizontal_poster = GlobalFunction::saveFileAndGivePath($request->file('horizontal_poster'));
		} else {
			$horizontal_poster = $hidden_horizontal_poster;
		}
		$data['horizontal_poster'] = $horizontal_poster;

		$data['content_title'] = $content_title;
		$data['description'] = $description;
		$data['content_type'] = $content_type;
		$data['duration'] = $duration;
		$data['release_year'] = $release_year;
		$data['ratings'] = $ratings;
		$data['language_id'] = $language_id;
		$data['genre_id'] = implode(',', $genre_id);
		$data['download_link'] = $download_link;
		$data['trailer_url'] = $trailer_url ? $trailer_url : "";

		if ($action == 'update') {
			$result =  Content::where('content_id', $content_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$data['content_id'] = $content_id;
			$result =  Content::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}

		if ($result) {
			if ($is_notify == 1) {
				$users = User::where('status', 1)->get();
				foreach ($users as $value) {
					$settings = Settings::first();
					$notification_title = $settings->app_name;
					$message = "New Movie Added " . $content_id;
					$is_send = Common::send_push($value['device_token'], $notification_title, $message, $value['device_type'], $image = "", 0);

					if ($is_send) {

						$notificationdata = array(
							'title' => $notification_title,
							'message' => $message,
							// 'icon' => $imagename
						);
						Notification::insert($notificationdata);
					}
				}
			}


			$response['success'] = 1;
			$response['content_id'] = $content_id;
			$response['content_type'] = $content_type;
			$response['message'] = "Successfully " . $msg . " Content";
		} else {
			$response['success'] = 0;
			$response['content_id'] = 0;
			$response['content_type'] = 0;
			$response['message'] = "Error While " . $msg . " Content";
		}
		echo json_encode($response);
	}

	public function changeFeatureStatus(Request $request)
	{
		$content_id = $request->input('content_id');
		$status = $request->input('status');

		$result =  Content::where('content_id', $content_id)->update(['is_featured' => $status]);
		$total_content = Content::count();
		$total_movie = Content::where('content_type', 1)->count();
		$total_series = Content::where('content_type', 2)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_content'] = $total_content;
			$response['total_movie'] = $total_movie;
			$response['total_series'] = $total_series;
		} else {
			$response['success'] = 0;
			$response['total_content'] = 0;
			$response['total_movie'] = 0;
			$response['total_series'] = 0;
		}

		echo json_encode($response);
	}


	public function deleteContent(Request $request)
	{

		$content_id = $request->input('content_id');
		$contentData = Content::where('content_id', $content_id)->first();
		if ($contentData->verticle_poster) {
			if (file_exists(env('API_PATH') . 'uploads/' . $contentData->verticle_poster)) {
				unlink(env('API_PATH') . 'uploads/' . $contentData->verticle_poster);
			}
		}
		if ($contentData->horizontal_poster) {
			if (file_exists(env('API_PATH') . 'uploads/' . $contentData->horizontal_poster)) {
				unlink(env('API_PATH') . 'uploads/' . $contentData->horizontal_poster);
			}
		}
		$result =  ContentSource::where('content_id', $content_id)->delete();
		$result =  ContentSubtitles::where('content_id', $content_id)->delete();
		Comment::where('content_id', $content_id)->delete();
		Watchlist::where('content_id', $content_id)->delete();
		$total_content = Content::count();
		$total_movie = Content::where('content_type', 1)->count();
		$total_series = Content::where('content_type', 2)->count();
		$result =  Content::where('content_id', $content_id)->delete();

		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully Delete Content";
			$response['total_content'] = $total_content;
			$response['total_movie'] = $total_movie;
			$response['total_series'] = $total_series;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While Delete Content";
			$response['total_content'] = 0;
			$response['total_movie'] = 0;
			$response['total_series'] = 0;
		}
		echo json_encode($response);
	}


	public function showContentList(Request $request)
	{

		$columns = array(
			0 => 'content_id',
			1 => 'content_id',
			2 => 'content_title',
			3 => 'ratings',
			4 => 'release_year',
			5 => 'language_name',
			6 => 'is_featured',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$content_type = $request->input("content_type");

		if (empty($request->input('search.value'))) {
			$query  = Content::select('tbl_content.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_content.language_id');
			if ($content_type == 1) {
				$query->where('content_type', 1);
			} else
            if ($content_type == 2) {
				$query->where('content_type', 2);
			}
			$ContentData = $query->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query  = Content::select('tbl_content.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_content.language_id');
			if ($content_type == 1) {
				$query->where('content_type', 1);
			} else
            if ($content_type == 2) {
				$query->where('content_type', 2);
			}
			$totalData = $totalFiltered = $query->count();
		} else {
			$search = $request->input('search.value');
			$query =  Content::select('tbl_content.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_content.language_id');
			if ($content_type == 1) {
				$query->where('tbl_content.content_type', 1);
			} else
			if ($content_type == 2) {
				$query->where('tbl_content.content_type', 2);
			}
			$query->where('tbl_content.content_id', 'LIKE', "%{$search}%")->orWhere('tbl_content.content_title', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.description', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.duration', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.release_year', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.ratings', 'LIKE', "%{$search}%")
				->orWhere('l.language_name', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.download_link', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.trailer_url', 'LIKE', "%{$search}%");

			$ContentData = $query->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  Content::select('tbl_content.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 					'tbl_content.language_id');
			if ($content_type == 1) {
				$query->where('tbl_content.content_type', 1);
			} else
							if ($content_type == 2) {
				$query->where('tbl_content.content_type', 2);
			}
			$query->where('tbl_content.content_id', 'LIKE', "%{$search}%")->orWhere('tbl_content.content_title', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.description', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.duration', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.release_year', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.ratings', 'LIKE', "%{$search}%")
				->orWhere('l.language_name', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.download_link', 'LIKE', "%{$search}%")
				->orWhere('tbl_content.trailer_url', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->count();
		}

		$data = array();
		if (!empty($ContentData)) {
			foreach ($ContentData as $rows) {

				if (!empty($rows->verticle_poster)) {
					$verticle_poster = '<img height="60" width="60" src="' . url(env('DEFAULT_IMAGE_URL') . $rows->verticle_poster) . '">';
				} else {
					$verticle_poster = '<img height="60px;" width="60px;" src="' . asset('assets/dist/img/default.png') . '">';
				}

				if (!empty($rows->horizontal_poster)) {
					$horizontal_poster = '<img height="60" width="60" src="' . url(env('DEFAULT_IMAGE_URL') . $rows->horizontal_poster) . '">';
				} else {
					$horizontal_poster = '<img height="60px;" width="60px;" src="' . asset('assets/dist/img/default.png') . '">';
				}


				if ($rows->is_featured == 1) {
					$checked = 'checked';
				} else {
					$checked = '';
				}

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}

				$is_featured_status = '<label class="switch"> <input type="checkbox" id="changeFeatureStatus" data-id="' . $rows->content_id . '" data-status="' . $rows->is_featured . '" ' . $checked . ' > <span class="slider round"></span> </label>';

				$view =  route('content/view', $rows->content_id);
				$edit =  route('content/edit', [$rows->content_type, $rows->content_id]);

				$data[] = array(
					$verticle_poster,
					$horizontal_poster,
					$rows->content_title,
					$rows->ratings,
					$rows->release_year,
					$rows->language_name,
					$is_featured_status,
					'<a class="edit text-light" href="' . $edit . '" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a> <a class="delete DeleteContent" data-id="' . $rows->content_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}


	public function viewContentSource($content_type, $content_id)
	{
		$contentData = Content::where('content_id', $content_id)->first();
		if ($contentData['total_view']) {
			$contentData['total_view'] = Common::number_format_short($contentData['total_view']);
		}
		if ($contentData['total_download']) {
			$contentData['total_download'] = Common::number_format_short($contentData['total_download']);
		}
		if ($contentData['total_share']) {
			$contentData['total_share'] = Common::number_format_short($contentData['total_share']);
		}
		$total_source = ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->count();
		return view('admin.content.source_addupdate')->with('total_source', $total_source)->with('title', 'Edit')->with('content_type', $content_type)->with('content_id', $content_id)->with('data', $contentData)->with('content_title', $contentData['content_title']);
	}

	public function UploadContentSourceMedia(Request $request)
	{

		$imageFileName = "";
		if ($request->hasfile('source_video')) {
			$imageFileName = GlobalFunction::saveFileAndGivePath($request->file('source_video'));
		}

		if ($imageFileName) {
			$response['success'] = 1;
			$response['default_path'] = url(env('DEFAULT_IMAGE_URL'));
			$response['source_video'] = $imageFileName;
		} else {
			$response['success'] = 0;
			$response['default_path'] = "";
			$response['source_video'] = "";
		}
		echo json_encode($response);
	}

	public function addUpdateContentSource(Request $request)
	{
		$source_id = $request->input('source_id');
		$content_id = $request->input('content_id');
		$content_type = $request->input('content_type');
		$source_title = $request->input('source_title');
		$source_quality = $request->input('source_quality');
		$source_size = $request->input('source_size');
		$downloadable = $request->input('downloadable');
		$access_type = $request->input('access_type');
		$source_type = $request->input('source_type');
		$source = $request->input('source');
		$source_file = $request->input('source_file');

		if ($source_type == 7) {
			$data['source'] = $source_file;
		} else {
			$data['source'] = $source;
		}

		$data['content_id'] = $content_id;
		$data['content_type'] = $content_type;
		$data['source_title'] = $source_title;
		$data['source_quality'] = $source_quality;
		$data['source_size'] = $source_size;
		$data['downloadable'] = ($downloadable == 'on') ? 1 : 0;
		$data['access_type'] = $access_type;
		$data['source_type'] = $source_type;

		if (!empty($source_id)) {
			$result =  ContentSource::where('source_id', $source_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  ContentSource::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_source = ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Content Source";
			$response['total_source'] = $total_source;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Content Source";
			$response['total_source'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteContentSource(Request $request)
	{
		$content_type = $request->input('content_type');
		$source_id = $request->input('source_id');
		$content_id = $request->input('content_id');
		$sourceData = ContentSource::where('source_id', $source_id)->first();
		if ($sourceData && $sourceData->source && $sourceData->source_type == 7 && file_exists(public_path('uploads/') . $sourceData->source)) {
			unlink(public_path('uploads/') . $sourceData->source);
		}

		$result = ContentSource::where('source_id', $source_id)->delete();
		$total_source = ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_source'] = $total_source;
		} else {
			$response['success'] = 0;
			$response['total_source'] = 0;
		}
		echo json_encode($response);
	}


	public function showContentSourceList(Request $request)
	{

		$columns = array(
			0 => 'source_type',
			1 => 'source_title',
			2 => 'source',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$content_type = $request->input("content_type");
		$content_id = $request->input("content_id");

		if (empty($request->input('search.value'))) {
			$ContentSourceData  = ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->count();
		} else {
			$search = $request->input('search.value');
			$query =  ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->where('source_id', 'LIKE', "%{$search}%")
				->orWhere('source_title', 'LIKE', "%{$search}%")
				->orWhere('source_type', 'LIKE', "%{$search}%")
				->orWhere('source', 'LIKE', "%{$search}%");

			$ContentSourceData = $query->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  ContentSource::where('content_id', $content_id)->where('content_type', $content_type)->where('source_id', 'LIKE', "%{$search}%")
				->orWhere('source_title', 'LIKE', "%{$search}%")
				->orWhere('source_type', 'LIKE', "%{$search}%")
				->orWhere('source', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->count();
		}

		$data = array();
		if (!empty($ContentSourceData)) {
			foreach ($ContentSourceData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}

				if ($rows->source_type == 1) {
					$source_label = 'Youtube Id';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 2) {
					$source_label = 'M3u8 Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 3) {
					$source_label = 'Mov Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 4) {
					$source_label = 'Mp4 Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 5) {
					$source_label = 'Mkv Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 6) {
					$source_label = 'Webm Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 7) {
					$source_label = 'File';
					$source = url(env('DEFAULT_IMAGE_URL') . $rows->source);
					$source_html = '<button data-toggle="modal" data-target="#modal-video" data-src="' . url(env('DEFAULT_IMAGE_URL') . $rows->source) . '" class="btn btn-success text-white" id="playvideomdl" title="Play Video"><i class="fa fa-play" style="font-size: 14px;" ></i></button>';
				}

				$data[] = array(
					$source_label,
					$rows->source_title,
					$source_html,
					'<a class="updateContentSource"  data-toggle="modal" data-target="#SourceModal" data-id="' . $rows->source_id . '" data-source_title="' . $rows->source_title . '" data-source_quality="' . $rows->source_quality . '" data-source_size="' . $rows->source_size . '" data-downloadable="' . $rows->downloadable . '" data-access_type="' . $rows->access_type . '" data-source_type="' . $rows->source_type . '" data-source="' . $source . '" data-source_video="' . $rows->source . '" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a> <a class="delete DeleteContentSource" data-id="' . $rows->source_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}


	public function viewAddMovieCast($content_type, $content_id)
	{
		$contentData = Content::where('content_id', $content_id)->first();
		if ($contentData['total_view']) {
			$contentData['total_view'] = Common::number_format_short($contentData['total_view']);
		}
		if ($contentData['total_download']) {
			$contentData['total_download'] = Common::number_format_short($contentData['total_download']);
		}
		if ($contentData['total_share']) {
			$contentData['total_share'] = Common::number_format_short($contentData['total_share']);
		}
		$actorData = Actor::get();
		$total_cast = MovieCast::where('content_id', $content_id)->count();
		return view('admin.content.cast_addupdate')->with('total_cast', $total_cast)->with('actorData', $actorData)->with('title', 'Edit')->with('content_type', $content_type)->with('content_id', $content_id)->with('data', $contentData)->with('content_title', $contentData['content_title']);
	}

	public function CheckExistMCastActor(Request $request)
	{
		$movie_cast_id = $request->input('movie_cast_id');
		$actor_id = $request->input('actor_id');
		$content_id = $request->input('content_id');

		if (!empty($movie_cast_id)) {
			$checkActor = MovieCast::selectRaw('*')->where('actor_id', $actor_id)->where('content_id', $content_id)->where('movie_cast_id', '!=', $movie_cast_id)->first();
		} else {
			$checkActor = MovieCast::selectRaw('*')->where('actor_id', $actor_id)->where('content_id', $content_id)->first();
		}

		if (!empty($checkActor)) {
			return json_encode(FALSE);
		} else {
			return json_encode(TRUE);
		}
	}

	public function addUpdateMovieCast(Request $request)
	{
		$movie_cast_id = $request->input('movie_cast_id');
		$content_id = $request->input('content_id');
		$actor_id = $request->input('actor_id');
		$charactor_name = $request->input('charactor_name');

		$data['content_id'] = $content_id;
		$data['actor_id'] = $actor_id;
		$data['charactor_name'] = $charactor_name;

		if (!empty($movie_cast_id)) {
			$result =  MovieCast::where('movie_cast_id', $movie_cast_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  MovieCast::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_cast = MovieCast::where('content_id', $content_id)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Movie Cast";
			$response['total_cast'] = $total_cast;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Movie Cast";
			$response['total_cast'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteMovieCast(Request $request)
	{
		$movie_cast_id = $request->input('movie_cast_id');
		$content_id = $request->input('content_id');
		$result = MovieCast::where('movie_cast_id', $movie_cast_id)->delete();
		$total_cast = MovieCast::where('content_id', $content_id)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_cast'] = $total_cast;
		} else {
			$response['success'] = 0;
			$response['total_cast'] = 0;
		}
		echo json_encode($response);
	}

	public function showMovieCastList(Request $request)
	{

		$columns = array(
			0 => 'movie_cast_id',
			1 => 'actor_name',
			2 => 'charactor_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$content_id = $request->input("content_id");

		if (empty($request->input('search.value'))) {
			$MovieCastData  = MovieCast::select('tbl_movie_cast.*', 'a.actor_name', 'a.actor_image')->leftjoin('tbl_actor as a', 'a.actor_id', 'tbl_movie_cast.actor_id')->where('tbl_movie_cast.content_id', $content_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = MovieCast::count();
		} else {
			$search = $request->input('search.value');
			$query =  MovieCast::select('tbl_movie_cast.*', 'a.actor_name', 'a.actor_image')->leftjoin('tbl_actor as a', 'a.actor_id', 'tbl_movie_cast.actor_id')->where('tbl_movie_cast.content_id', $content_id)->where('tbl_movie_cast.movie_cast_id', 'LIKE', "%{$search}%")
				->orWhere('tbl_movie_cast.charactor_name', 'LIKE', "%{$search}%")
				->orWhere('a.actor_name', 'LIKE', "%{$search}%");

			$MovieCastData = $query->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  MovieCast::select('tbl_movie_cast.*', 'a.actor_name', 'a.actor_image')->leftjoin('tbl_actor as a', 'a.actor_id', 'tbl_movie_cast.actor_id')->where('tbl_movie_cast.content_id', $content_id)->where('tbl_movie_cast.movie_cast_id', 'LIKE', "%{$search}%")
				->orWhere('tbl_movie_cast.charactor_name', 'LIKE', "%{$search}%")
				->orWhere('a.actor_name', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->count();
		}

		$data = array();
		if (!empty($MovieCastData)) {
			foreach ($MovieCastData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}

				if (!empty($rows->actor_image)) {
					$profile = '<img height="60" width="60" src="' . url(env('DEFAULT_IMAGE_URL') . $rows->actor_image) . '">';
				} else {
					$profile = '<img height="60px;" width="60px;" src="' . asset('assets/dist/img/default.png') . '">';
				}

				$data[] = array(
					$profile,
					$rows->actor_name,
					$rows->charactor_name,
					'<a class="updateMovieCast"  data-toggle="modal" data-target="#MovieCastModal"  data-id="' . $rows->movie_cast_id . '" data-actor_id="' . $rows->actor_id . '" data-image="' . url(env('DEFAULT_IMAGE_URL') . $rows->actor_image) . '" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a> <a class="delete DeleteMovieCast" data-id="' . $rows->movie_cast_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}


	public function viewAddContentSubtitles($content_type, $content_id)
	{
		$contentData = Content::where('content_id', $content_id)->first();
		if ($contentData['total_view']) {
			$contentData['total_view'] = Common::number_format_short($contentData['total_view']);
		}
		if ($contentData['total_download']) {
			$contentData['total_download'] = Common::number_format_short($contentData['total_download']);
		}
		if ($contentData['total_share']) {
			$contentData['total_share'] = Common::number_format_short($contentData['total_share']);
		}
		$languagedata = Language::get();
		$total_subtitles = ContentSubtitles::where('content_id', $content_id)->where('content_type', $content_type)->count();
		return view('admin.content.subtitles_addupdate')->with('total_subtitles', $total_subtitles)->with('languagedata', $languagedata)->with('title', 'Edit')->with('content_type', $content_type)->with('content_id', $content_id)->with('data', $contentData)->with('content_title', $contentData['content_title']);
	}

	public function addUpdateContentSubtitles(Request $request)
	{
		$subtitles_id = $request->input('subtitles_id');
		$content_type = $request->input('content_type');
		$content_id = $request->input('content_id');
		$language_id = $request->input('language_id');
		if ($request->hasfile('subtitle_file')) {
			$data['subtitle_file'] = GlobalFunction::saveFileAndGivePath($request->file('subtitle_file'));;
		}

		$data['content_type'] = $content_type;
		$data['content_id'] = $content_id;
		$data['language_id'] = $language_id;

		if (!empty($subtitles_id)) {
			$result =  ContentSubtitles::where('subtitles_id', $subtitles_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  ContentSubtitles::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_subtitles = ContentSubtitles::where('content_id', $content_id)->where('content_type', $content_type)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Subtitles";
			$response['total_subtitles'] = $total_subtitles;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Subtitles";
			$response['total_subtitles'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteContentSubtitles(Request $request)
	{
		$subtitles_id = $request->input('subtitles_id');
		$content_type = $request->input('content_type');
		$content_id = $request->input('content_id');
		$SubtitlesData = ContentSubtitles::where('subtitles_id', $subtitles_id)->first();
		if ($SubtitlesData && $SubtitlesData->subtitle_file && file_exists(public_path('uploads/') . $SubtitlesData->subtitle_file)) {
			unlink(public_path('uploads/') . $SubtitlesData->subtitle_file);
		}

		$result = ContentSubtitles::where('subtitles_id', $subtitles_id)->delete();
		$total_subtitles = ContentSubtitles::where('content_id', $content_id)->where('content_type', $content_type)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_subtitles'] = $total_subtitles;
		} else {
			$response['success'] = 0;
			$response['total_subtitles'] = 0;
		}
		echo json_encode($response);
	}


	public function showContentSubtitlesList(Request $request)
	{

		$columns = array(
			0 => 'language_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$content_type = $request->input("content_type");
		$content_id = $request->input("content_id");

		if (empty($request->input('search.value'))) {
			$ContentSubtitlesData  = ContentSubtitles::select('tbl_content_subtitles.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_content_subtitles.language_id')->where('tbl_content_subtitles.content_id', $content_id)->where('tbl_content_subtitles.content_type', $content_type)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = ContentSubtitles::count();
		} else {
			$search = $request->input('search.value');
			$query =  ContentSubtitles::select('tbl_content_subtitles.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_content_subtitles.language_id')->where('tbl_content_subtitles.subtitles_id', 'LIKE', "%{$search}%")->orWhere('l.language_name', 'LIKE', "%{$search}%");

			$ContentSubtitlesData = $query->where('tbl_content_subtitles.content_id', $content_id)->where('tbl_content_subtitles.content_type', $content_type)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  ContentSubtitles::select('tbl_content_subtitles.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_content_subtitles.language_id')->where('tbl_content_subtitles.content_type', $content_type)->where('tbl_content_subtitles.subtitles_id', 'LIKE', "%{$search}%")->orWhere('l.language_name', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->where('tbl_content_subtitles.content_id', $content_id)->count();
		}

		$data = array();
		if (!empty($ContentSubtitlesData)) {
			foreach ($ContentSubtitlesData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}


				$data[] = array(
					$rows->language_name,
					'<a class="delete DeleteContentSubtitles" data-id="' . $rows->subtitles_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a> <a class="download" href="' . url(env('DEFAULT_IMAGE_URL') . $rows->subtitle_file) . '" target="_blank" ><i class="fa fa-download text-info font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}

	public function viewContentComment($content_type, $content_id)
	{
		$contentData = Content::where('content_id', $content_id)->first();
		if ($contentData['total_view']) {
			$contentData['total_view'] = Common::number_format_short($contentData['total_view']);
		}
		if ($contentData['total_download']) {
			$contentData['total_download'] = Common::number_format_short($contentData['total_download']);
		}
		if ($contentData['total_share']) {
			$contentData['total_share'] = Common::number_format_short($contentData['total_share']);
		}
		$total_comment = Comment::where('content_id', $content_id)->count();

		return view('admin.content.comment_list')->with('total_comment', $total_comment)->with('title', 'Edit')->with('content_type', $content_type)->with('content_id', $content_id)->with('data', $contentData)->with('content_title', $contentData['content_title']);
	}


	public function changeCommentStatus(Request $request)
	{
		$content_id = $request->input('content_id');
		$comment_id = $request->input('comment_id');
		$status = $request->input('status');

		$result =  Comment::where('comment_id', $comment_id)->where('content_id', $content_id)->update(['status' => $status]);
		$total_comment = Comment::where('content_id', $content_id)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_comment'] = $total_comment;
		} else {
			$response['success'] = 0;
			$response['total_comment'] = 0;
		}

		echo json_encode($response);
	}


	public function deleteComment(Request $request)
	{
		$content_id = $request->input('content_id');
		$comment_id = $request->input('comment_id');
		$result =  Comment::where('comment_id', $comment_id)->where('content_id', $content_id)->delete();
		$total_comment = Comment::where('content_id', $content_id)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully Delete Comment";
			$response['total_comment'] = $total_comment;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While Delete Comment";
			$response['total_comment'] = 0;
		}
		echo json_encode($response);
	}

	public function showContentCommentList(Request $request)
	{

		$columns = array(
			0 => 'comment_id',
			1 => 'fullname',
			2 => 'comment',
			3 => 'created_at',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$content_id = $request->input("content_id");

		if (empty($request->input('search.value'))) {
			$CommentData  = Comment::select('tbl_comment.*', 'u.fullname', 'u.profile_image')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_comment.user_id')->where('tbl_comment.content_id', $content_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = Comment::count();
		} else {
			$search = $request->input('search.value');
			$query =  Comment::select('tbl_comment.*', 'u.fullname', 'u.profile_image')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_comment.user_id')->where('tbl_comment.content_id', $content_id)->where('tbl_comment.comment_id', 'LIKE', "%{$search}%")
				->orWhere('tbl_comment.comment', 'LIKE', "%{$search}%")
				->orWhere('u.fullname', 'LIKE', "%{$search}%");

			$CommentData = $query->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  Comment::select('tbl_comment.*', 'u.fullname', 'u.profile_image')->leftjoin('tbl_users as u', 'u.user_id', 'tbl_comment.user_id')->where('tbl_comment.content_id', $content_id)->where('tbl_comment.comment_id', 'LIKE', "%{$search}%")
				->orWhere('tbl_comment.comment', 'LIKE', "%{$search}%")
				->orWhere('u.fullname', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->count();
		}

		$data = array();
		if (!empty($CommentData)) {
			foreach ($CommentData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}

				if (!empty($rows->profile_image)) {
					$profile = '<img height="60" width="60" src="' . url(env('DEFAULT_IMAGE_URL') . $rows->profile_image) . '">';
				} else {
					$profile = '<img height="60px;" width="60px;" src="' . asset('assets/dist/img/default.png') . '">';
				}
				if ($rows->status == 1) {
					$showhidehtml = '<a class="delete showHideComment" data-content_id="' . $rows->content_id . '" data-id="' . $rows->comment_id . '" data-status="' . $rows->status . '" ><i class="fa fa-eye text-success font-20 pointer p-l-5 p-r-5"></i></a>';
				} else {
					$showhidehtml = '<a class="delete showHideComment" data-content_id="' . $rows->content_id . '" data-id="' . $rows->comment_id . '" data-status="' . $rows->status . '" ><i class="fa fa-eye-slash text-success font-20 pointer p-l-5 p-r-5"></i></a>';
				}
				$added = Common::time_elapsed_string($rows->created_at);
				$data[] = array(
					$profile,
					$rows->fullname,
					$rows->comment,
					'<span class="badge badge-dark">' . $added . '</span>',
					'<a class="delete DeleteComment" data-content_id="' . $rows->content_id . '" data-id="' . $rows->comment_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>' . $showhidehtml
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}

	public function viewAddSeriesSeason($content_type, $content_id)
	{
		$contentData = Content::where('content_id', $content_id)->first();
		if ($contentData['total_view']) {
			$contentData['total_view'] = Common::number_format_short($contentData['total_view']);
		}
		if ($contentData['total_download']) {
			$contentData['total_download'] = Common::number_format_short($contentData['total_download']);
		}
		if ($contentData['total_share']) {
			$contentData['total_share'] = Common::number_format_short($contentData['total_share']);
		}
		$total_season = SeriesSeason::where('content_id', $content_id)->count();
		$seasondata = SeriesSeason::where('content_id', $content_id)->get();
		return view('admin.content.season_addupdate')->with('total_season', $total_season)->with('title', 'Edit')->with('content_type', $content_type)->with('content_id', $content_id)->with('data', $contentData)->with('content_title', $contentData['content_title'])->with('seasondata', $seasondata);
	}

	public function CheckExistseason(Request $request)
	{
		$season_id = $request->input('season_id');
		$season_title = $request->input('season_title');

		$content_id = $request->input('content_id');

		if (!empty($season_id)) {
			$checkseason = SeriesSeason::selectRaw('*')->where('season_title', $season_title)->where('content_id', $content_id)->where('season_id', '!=', $season_id)->where('season_id', '!=', $season_id)->first();
		} else {
			$checkseason = SeriesSeason::selectRaw('*')->where('season_title', $season_title)->where('content_id', $content_id)->first();
		}

		if (!empty($checkseason)) {
			return json_encode(FALSE);
		} else {
			return json_encode(TRUE);
		}
	}

	public function addUpdateSeriesSeason(Request $request)
	{
		$season_id = $request->input('season_id');
		$content_id = $request->input('content_id');
		$season_title = $request->input('season_title');
		$trailer_url = $request->input('trailer_url');

		$data['content_id'] = $content_id;
		$data['season_title'] = $season_title;
		$data['trailer_url'] = $trailer_url;

		if (!empty($season_id)) {
			$result =  SeriesSeason::where('season_id', $season_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  SeriesSeason::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
			$season_id = DB::getPdo()->lastInsertId();
		}
		$seasondata = SeriesSeason::where('content_id', $content_id)->get();
		$total_season = SeriesSeason::where('content_id', $content_id)->count();
		$insertSeasondata = SeriesSeason::where('season_id', $season_id)->where('content_id', $content_id)->first();

		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Season";
			$response['total_season'] = $total_season;
			$response['data'] = $seasondata;
			$response['new_data'] = $insertSeasondata;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Season";
			$response['total_season'] = 0;
			$response['data'] = [];
			$response['new_data'] = [];
		}
		echo json_encode($response);
	}

	public function deleteSeriesSeason(Request $request)
	{
		$season_id = $request->input('season_id');
		$content_id = $request->input('content_id');

		$result = SeriesSeason::where('season_id', $season_id)->delete();
		$total_season = SeriesSeason::where('content_id', $content_id)->count();
		$seasondata = SeriesSeason::where('content_id', $content_id)->get();

		if ($result) {
			$response['success'] = 1;
			$response['total_season'] = $total_season;
			$response['data'] = $seasondata;
		} else {
			$response['success'] = 0;
			$response['total_season'] = 0;
			$response['data'] = [];
		}
		echo json_encode($response);
	}


	public function viewAddEpisode($season_id = "")
	{
		$seasonData = SeriesSeason::where('season_id', $season_id)->first();
		$contentData = Content::where('content_id', $seasonData['content_id'])->first();

		if (Session::get('admin_id') == 1) {
			return view('admin.content.episode_addupdate')->with('data', [])->with('seasonData', $seasonData)->with('season_id', $season_id)->with('season_title', $seasonData['season_title'])->with('content_title', $contentData['content_title'])->with('episode_id', 0)->with('title', 'New');
		} else {
			return Redirect::route('dashboard');
		}
	}

	public function viewUpdateEpisode($season_id, $id)
	{
		$data = SeasonEpisode::where('episode_id', $id)->first();
		$seasonData = SeriesSeason::where('season_id', $season_id)->first();
		$contentData = Content::where('content_id', $seasonData['content_id'])->first();
		if ($data['total_view']) {
			$data['total_view'] = Common::number_format_short($data['total_view']);
		}
		if ($data['total_download']) {
			$data['total_download'] = Common::number_format_short($data['total_download']);
		}
		if (Session::get('admin_id') == 1) {
			return view('admin.content.episode_addupdate')->with('data', $data)->with('seasonData', $seasonData)->with('season_id', $season_id)->with('season_title', $seasonData['season_title'])->with('content_title', $contentData['content_title'])->with('episode_id', $id)->with('title', 'Edit');
		} else {
			return Redirect::route('dashboard');
		}
	}

	public function addUpdateSeasonEpisode(Request $request)
	{
		$episode_id = $request->input('episode_id');
		$season_id = $request->input('episode_season_id');
		$episode_title = $request->input('episode_title');
		$description = $request->input('description');
		$duration = $request->input('duration');
		$access_type = $request->input('access_type');

		if ($request->hasfile('episode_thumb')) {
			$data['episode_thumb'] = GlobalFunction::saveFileAndGivePath($request->file('episode_thumb'));
		}


		$data['season_id'] = $season_id;
		$data['episode_title'] = $episode_title;
		$data['episode_description'] = $description;
		$data['episode_duration'] = $duration;
		$data['access_type'] = $access_type;

		if (!empty($episode_id)) {
			$result =  SeasonEpisode::where('episode_id', $episode_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  SeasonEpisode::insert($data);
			$episode_id = DB::getPdo()->lastInsertId();

			$msg = "Add";
			$response['flag'] = 1;
		}

		$total_episode = SeasonEpisode::where('season_id', $season_id)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Episode";
			$response['total_episode'] = $total_episode;
			$response['episode_id'] = $episode_id;
			$response['season_id'] = $season_id;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Episode";
			$response['total_episode'] = 0;
			$response['episode_id'] = 0;
			$response['season_id'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteSeasonEpisode(Request $request)
	{
		$episode_id = $request->input('episode_id');
		$season_id = $request->input('season_id');
		$content_id = $request->input('content_id');

		$result = SeasonEpisode::where('episode_id', $episode_id)->delete();
		$total_episode = SeasonEpisode::where('season_id', $season_id)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_episode'] = $total_episode;
		} else {
			$response['success'] = 0;
			$response['total_episode'] = 0;
		}
		echo json_encode($response);
	}


	public function showSeasonEpisodeList(Request $request)
	{

		$columns = array(
			0 => 'episode_id',
			1 => 'episode_title',
			2 => 'episode_description',

		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$content_type = $request->input("content_type");
		$season_id = $request->input("season_id");

		if (empty($request->input('search.value'))) {
			$SeasonEpisodeData  = SeasonEpisode::where('season_id', $season_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = SeasonEpisode::count();
		} else {
			$search = $request->input('search.value');
			$query =  SeasonEpisode::where('season_id', $season_id)->where('episode_id', 'LIKE', "%{$search}%")->orWhere('episode_title', 'LIKE', "%{$search}%")->where('episode_description', 'LIKE', "%{$search}%");

			$SeasonEpisodeData = $query->where('season_id', $season_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  SeasonEpisode::where('season_id', $season_id)->where('season_id', $season_id)->where('episode_id', 'LIKE', "%{$search}%")->orWhere('episode_title', 'LIKE', "%{$search}%")->where('episode_description', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->where('season_id', $season_id)->count();
		}

		$data = array();
		if (!empty($SeasonEpisodeData)) {
			foreach ($SeasonEpisodeData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}
				if (!empty($rows->episode_thumb)) {
					$episode_thumb = '<img height="80px" width="80px" src="' . url(env('DEFAULT_IMAGE_URL') . $rows->episode_thumb) . '">';
				} else {
					$episode_thumb = '<img height="80px;" width="80px;" src="' . asset('assets/dist/img/default.png') . '">';
				}
				$edit =  route('series/season/episode/edit', [$rows->season_id, $rows->episode_id]);
				$data[] = array(
					$episode_thumb,
					$rows->episode_title,
					$rows->episode_description,
					'<a class="updateSeasonEpisode"  href="' . $edit . '" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a> <a class="delete DeleteSeasonEpisode" data-id="' . $rows->episode_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}


	public function viewEpisodeSource($season_id, $episode_id)
	{
		$seasonData = SeriesSeason::where('season_id', $season_id)->first();
		$contentData = Content::where('content_id', $seasonData['content_id'])->first();
		$data = SeasonEpisode::where('episode_id', $episode_id)->first();
		if ($data['total_view']) {
			$data['total_view'] = Common::number_format_short($data['total_view']);
		}
		if ($data['total_download']) {
			$data['total_download'] = Common::number_format_short($data['total_download']);
		}
		$total_source = EpisodeSource::where('episode_id', $episode_id)->count();
		return view('admin.content.episode_source_addupdate')->with('total_source', $total_source)->with('data', $data)->with('title', 'Edit')->with('episode_id', $episode_id)->with('season_id', $season_id)->with('seasonData', $seasonData)->with('content_title', $contentData['content_title']);
	}


	public function addUpdateEpisodeSource(Request $request)
	{
		$source_id = $request->input('source_id');
		$episode_id = $request->input('episode_id');
		$source_title = $request->input('source_title');
		$source_quality = $request->input('source_quality');
		$source_size = $request->input('source_size');
		$downloadable = $request->input('downloadable');
		$access_type = $request->input('access_type');
		$source_type = $request->input('source_type');
		$source = $request->input('source');
		$source_file = $request->input('source_file');

		if ($source_type == 7) {
			$data['source'] = $source_file;
		} else {
			$data['source'] = $source;
		}

		$data['episode_id'] = $episode_id;
		$data['source_title'] = $source_title;
		$data['source_quality'] = $source_quality;
		$data['source_size'] = $source_size;
		$data['downloadable'] = ($downloadable == 'on') ? 1 : 0;
		$data['access_type'] = $access_type;
		$data['source_type'] = $source_type;

		if (!empty($source_id)) {
			$result =  EpisodeSource::where('source_id', $source_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  EpisodeSource::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_source = EpisodeSource::where('episode_id', $episode_id)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Content Source";
			$response['total_source'] = $total_source;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Content Source";
			$response['total_source'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteEpisodeSource(Request $request)
	{
		$source_id = $request->input('source_id');
		$episode_id = $request->input('episode_id');
		$sourceData = EpisodeSource::where('source_id', $source_id)->first();
		if ($sourceData && $sourceData->source_type == 7 && file_exists(public_path('uploads/') . $sourceData->source)) {
			unlink(public_path('uploads/') . $sourceData->source);
		}

		$result = EpisodeSource::where('source_id', $source_id)->delete();
		$total_source = EpisodeSource::where('episode_id', $episode_id)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_source'] = $total_source;
		} else {
			$response['success'] = 0;
			$response['total_source'] = 0;
		}
		echo json_encode($response);
	}

	public function showEpisodeSourceList(Request $request)
	{

		$columns = array(
			0 => 'source_type',
			1 => 'source_title',
			2 => 'source',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$episode_id = $request->input("episode_id");

		if (empty($request->input('search.value'))) {
			$EpisodeSourceData  = EpisodeSource::where('episode_id', $episode_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = EpisodeSource::where('episode_id', $episode_id)->count();
		} else {
			$search = $request->input('search.value');
			$query =  EpisodeSource::where('episode_id', $episode_id)->where('source_id', 'LIKE', "%{$search}%")
				->orWhere('source_title', 'LIKE', "%{$search}%")
				->orWhere('source_type', 'LIKE', "%{$search}%")
				->orWhere('source', 'LIKE', "%{$search}%");

			$EpisodeSourceData = $query->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  EpisodeSource::where('episode_id', $episode_id)->where('source_id', 'LIKE', "%{$search}%")
				->orWhere('source_title', 'LIKE', "%{$search}%")
				->orWhere('source_type', 'LIKE', "%{$search}%")
				->orWhere('source', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->count();
		}

		$data = array();
		if (!empty($EpisodeSourceData)) {
			foreach ($EpisodeSourceData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}

				if ($rows->source_type == 1) {
					$source_label = 'Youtube Id';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 2) {
					$source_label = 'M3u8 Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 3) {
					$source_label = 'Mov Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 4) {
					$source_label = 'Mp4 Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 5) {
					$source_label = 'Mkv Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 6) {
					$source_label = 'Webm Url';
					$source = $source_html = $rows->source;
				}
				if ($rows->source_type == 7) {
					$source_label = 'File';
					$source = url(env('DEFAULT_IMAGE_URL') . $rows->source);
					$source_html = '<button data-toggle="modal" data-target="#modal-video" data-src="' . url(env('DEFAULT_IMAGE_URL') . $rows->source) . '" class="btn btn-success text-white" id="playvideomdl" title="Play Video"><i class="fa fa-play" style="font-size: 14px;" ></i></button>';
				}

				$data[] = array(
					$source_label,
					$rows->source_title,
					$source_html,
					'<a class="updateEpisodeSource"  data-toggle="modal" data-target="#SourceModal" data-id="' . $rows->source_id . '" data-source_title="' . $rows->source_title . '" data-source_quality="' . $rows->source_quality . '" data-source_size="' . $rows->source_size . '" data-downloadable="' . $rows->downloadable . '" data-access_type="' . $rows->access_type . '" data-source_type="' . $rows->source_type . '" data-source="' . $source . '" data-source_video="' . $rows->source . '" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a> <a class="delete DeleteEpisodeSource" data-id="' . $rows->source_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}


	public function viewEpisodeSubtitles($season_id, $episode_id)
	{
		$seasonData = SeriesSeason::where('season_id', $season_id)->first();
		$languagedata = Language::get();
		$total_subtitles = EpisodeSubtitles::where('episode_id', $episode_id)->count();
		$contentData = Content::where('content_id', $seasonData['content_id'])->first();
		$data = SeasonEpisode::where('episode_id', $episode_id)->first();
		if ($data['total_view']) {
			$data['total_view'] = Common::number_format_short($data['total_view']);
		}
		if ($data['total_download']) {
			$data['total_download'] = Common::number_format_short($data['total_download']);
		}
		return view('admin.content.episode_subtitles_addupdate')->with('total_subtitles', $total_subtitles)->with('data', $data)->with('languagedata', $languagedata)->with('title', 'Edit')->with('season_id', $season_id)->with('seasonData', $seasonData)->with('episode_id', $episode_id)->with('content_title', $contentData['content_title']);
	}

	public function addUpdateEpisodeSubtitles(Request $request)
	{
		$subtitles_id = $request->input('subtitles_id');
		$episode_id = $request->input('episode_id');
		$language_id = $request->input('language_id');

		$imageFileName = "";
		if ($request->hasfile('subtitle_file')) {
			$data['subtitle_file'] = GlobalFunction::saveFileAndGivePath($request->file('subtitle_file'));
		}

		$data['episode_id'] = $episode_id;
		$data['language_id'] = $language_id;

		if (!empty($subtitles_id)) {
			$result =  EpisodeSubtitles::where('subtitles_id', $subtitles_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result =  EpisodeSubtitles::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_subtitles = EpisodeSubtitles::where('episode_id', $episode_id)->count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully " . $msg . " Subtitles";
			$response['total_subtitles'] = $total_subtitles;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While " . $msg . " Subtitles";
			$response['total_subtitles'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteEpisodeSubtitles(Request $request)
	{
		$subtitles_id = $request->input('subtitles_id');
		$episode_id = $request->input('episode_id');
		$SubtitlesData = EpisodeSubtitles::where('subtitles_id', $subtitles_id)->first();
		if ($SubtitlesData && $SubtitlesData->subtitle_file && file_exists(public_path('uploads/') . $SubtitlesData->subtitle_file)) {
			unlink(public_path('uploads/') . $SubtitlesData->subtitle_file);
		}

		$result = EpisodeSubtitles::where('subtitles_id', $subtitles_id)->delete();
		$total_subtitles = EpisodeSubtitles::where('episode_id', $episode_id)->count();

		if ($result) {
			$response['success'] = 1;
			$response['total_subtitles'] = $total_subtitles;
		} else {
			$response['success'] = 0;
			$response['total_subtitles'] = 0;
		}
		echo json_encode($response);
	}


	public function showEpisodeSubtitlesList(Request $request)
	{

		$columns = array(
			0 => 'language_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');
		$episode_id = $request->input("episode_id");

		if (empty($request->input('search.value'))) {
			$EpisodeSubtitlesData  = EpisodeSubtitles::select('tbl_episode_subtitles.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_episode_subtitles.language_id')->where('tbl_episode_subtitles.episode_id', $episode_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData = $totalFiltered = EpisodeSubtitles::count();
		} else {
			$search = $request->input('search.value');
			$query =  EpisodeSubtitles::select('tbl_episode_subtitles.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_episode_subtitles.language_id')->where('tbl_episode_subtitles.subtitles_id', 'LIKE', "%{$search}%")->orWhere('l.language_name', 'LIKE', "%{$search}%");

			$EpisodeSubtitlesData = $query->where('tbl_episode_subtitles.episode_id', $episode_id)->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$query =  EpisodeSubtitles::select('tbl_episode_subtitles.*', 'l.language_name')->leftjoin('tbl_language as l', 'l.language_id', 'tbl_episode_subtitles.language_id')->where('tbl_episode_subtitles.subtitles_id', 'LIKE', "%{$search}%")->orWhere('l.language_name', 'LIKE', "%{$search}%");

			$totalData = $totalFiltered = $query->where('tbl_episode_subtitles.episode_id', $episode_id)->count();
		}

		$data = array();
		if (!empty($EpisodeSubtitlesData)) {
			foreach ($EpisodeSubtitlesData as $rows) {

				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}


				$data[] = array(
					$rows->language_name,
					'<a class="delete DeleteEpisodeSubtitles" data-id="' . $rows->subtitles_id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a> <a class="download" href="' . url(env('DEFAULT_IMAGE_URL') . $rows->subtitle_file) . '" target="_blank" ><i class="fa fa-download text-info font-20 pointer p-l-5 p-r-5"></i></a>'
				);
			}
		}
		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}
}
