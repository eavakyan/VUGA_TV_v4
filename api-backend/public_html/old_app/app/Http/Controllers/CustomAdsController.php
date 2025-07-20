<?php

namespace App\Http\Controllers;

use App\CustomAdImages;
use App\CustomAds;
use App\CustomAdVideos;
use App\GlobalFunction;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Validator;

class CustomAdsController extends Controller
{
    //
    function increaseAdClick(Request $req)
    {
        $rules = [
            'ad_id' => 'required',

        ];
        $validator = Validator::make($req->all(), $rules);
        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }
        $ad = CustomAds::where('id', $req->ad_id)->first();
        if ($ad == null) {
            return response()->json(['status' => false, 'message' => "Ad doesn't exists !"]);
        }
        $ad->clicks = $ad->clicks + 1;
        $ad->save();
        return response()->json(['status' => true, 'message' => "Ad Click increased successfully !"]);
    }
    function increaseAdView(Request $req)
    {
        $rules = [
            'ad_id' => 'required',

        ];
        $validator = Validator::make($req->all(), $rules);
        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }
        $ad = CustomAds::where('id', $req->ad_id)->first();
        if ($ad == null) {
            return response()->json(['status' => false, 'message' => "Ad doesn't exists !"]);
        }
        $ad->views = $ad->views + 1;
        $ad->save();
        return response()->json(['status' => true, 'message' => "Ad View increased successfully !"]);
    }

    function fetchCustomAds(Request $req)
    {
        $rules = [
            'device_type' => 'required',

        ];
        $validator = Validator::make($req->all(), $rules);
        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        if ($req->device_type == 1) {
            $ads = CustomAds::with('adImages')->with('adVideos')->where('is_android', 1)->where('status', 1)->whereDate('end_date', '>=', date('Y-m-d'))->whereDate('start_date', '<=', date('Y-m-d'))->inRandomOrder()->limit(20)->get();
        }
        if ($req->device_type == 2) {
            $ads = CustomAds::with('adImages')->with('adVideos')->where('is_ios', 1)->where('status', 1)->whereDate('end_date', '>=', date('Y-m-d'))->whereDate('start_date', '<=', date('Y-m-d'))->inRandomOrder()->limit(20)->get();
        }

        return response()->json(['status' => true, 'message' => "Ads Data fetched successfully !", 'data' => $ads]);
    }
    function editAdVideo(Request $request)
    {
        $vid = CustomAdVideos::find($request->id);
        $vid->headline = $request->headline;
        $vid->description = $request->description;
        $vid->type = $request->type;
        if ($request->has('video')) {
            $vid->video = GlobalFunction::saveFileAndGivePath($request->file('video'));
        }
        $vid->save();
        return response()->json(['status' => true]);
    }
    function editAdImage(Request $request)
    {
        $img = CustomAdImages::find($request->id);
        $img->headline = $request->headline;
        $img->description = $request->description;
        $img->show_time = $request->show_time;
        if ($request->has('image')) {
            $img->image = GlobalFunction::saveFileAndGivePath($request->file('image'));
        }
        $img->save();
        return response()->json(['status' => true]);
    }
    function deleteAdImage(Request $request)
    {

        $img = CustomAdImages::find($request->id);
        $imgCount = CustomAdImages::where('ad_id', $img->ad->id)->count();
        $videoCount = CustomAdVideos::where('ad_id', $img->ad->id)->count();

        if (($imgCount + $videoCount) > 1) {
            CustomAdImages::find($request->id)->delete();
            return response()->json(['status' => true]);
        }
        return response()->json(['status' => false, 'message' => "Last resource can't be deleted"]);
    }
    function deleteAdVideo(Request $request)
    {

        $vid = CustomAdVideos::find($request->id);
        $imgCount = CustomAdImages::where('ad_id', $vid->ad->id)->count();
        $videoCount = CustomAdVideos::where('ad_id', $vid->ad->id)->count();
        if (($imgCount + $videoCount) > 1) {
            CustomAdVideos::find($request->id)->delete();
            return response()->json(['status' => true]);
        }

        return response()->json(['status' => false, 'message' => "Last resource can't be deleted"]);
    }

    function addVideoToAd(Request $request)
    {
        $vid = new CustomAdVideos();
        $vid->ad_id = $request->ad_id;
        $vid->headline = $request->headline;
        $vid->description = $request->description;
        $vid->type = $request->type;
        $vid->video = GlobalFunction::saveFileAndGivePath($request->file('video'));
        $vid->save();
        return response()->json(['status' => true]);
    }
    function addImageToAd(Request $request)
    {
        $img = new CustomAdImages();
        $img->ad_id = $request->ad_id;
        $img->headline = $request->headline;
        $img->description = $request->description;
        $img->show_time = $request->show_time;
        $img->image = GlobalFunction::saveFileAndGivePath($request->file('image'));
        $img->save();
        return response()->json(['status' => true]);
    }

    function showAdImagesList(Request $request)
    {
        $totalData =  CustomAdImages::where('ad_id', $request->adId)->count();
        $rows = CustomAdImages::where('ad_id', $request->adId)->orderBy('id', 'DESC')->get();

        $result = $rows;

        $columns = array(
            0 => 'id',
        );

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');

        $totalFiltered = $totalData;
        if (empty($request->input('search.value'))) {
            $result = CustomAdImages::where('ad_id', $request->adId)
                ->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
        } else {
            $search = $request->input('search.value');
            $result =  CustomAdImages::where('ad_id', $request->adId)
                ->where(function ($query) use ($search) {
                    $query->where('description', 'LIKE', "%{$search}%")
                        ->orWhere('headline', 'LIKE', "%{$search}%");
                })->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
            $totalFiltered = CustomAdImages::where('ad_id', $request->adId)
                ->where(function ($query) use ($search) {
                    $query->where('description', 'LIKE', "%{$search}%")
                        ->orWhere('headline', 'LIKE', "%{$search}%");
                })->count();
        }
        $data = array();
        foreach ($result as $item) {

            $imgURL = GlobalFunction::createMediaUrl($item->image);

            $thumb = '<img data-description="' . $item->description . '" data-headline="' . $item->headline . '" class="pointer show-img" src="' . $imgURL . '" width="50" height="50">';
            $delete = '<a href=""class=" btn btn-danger text-white delete-img " rel=' . $item->id . ' >Delete</a>';
            $edit = '<a data-image="' . $imgURL . '" data-show-time="' . $item->show_time . '" data-description="' . $item->description . '" data-headline="' . $item->headline . '" class=" btn btn-info text-white edit-img mr-2" rel=' . $item->id . ' >Edit</a>';

            $action = $edit . $delete;

            $data[] = array(
                $thumb,
                $item->headline,
                $item->description,
                $item->show_time,
                Carbon::createFromFormat('Y-m-d H:i:s', $item->created_at)->format('d M, Y'),
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
    function showAdVideoList(Request $request)
    {
        $totalData =  CustomAdVideos::where('ad_id', $request->adId)->count();
        $rows = CustomAdVideos::where('ad_id', $request->adId)->orderBy('id', 'DESC')->get();

        $result = $rows;

        $columns = array(
            0 => 'id',
        );

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');

        $totalFiltered = $totalData;
        if (empty($request->input('search.value'))) {
            $result = CustomAdVideos::where('ad_id', $request->adId)
                ->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
        } else {
            $search = $request->input('search.value');
            $result =  CustomAdVideos::where('ad_id', $request->adId)
                ->where(function ($query) use ($search) {
                    $query->where('description', 'LIKE', "%{$search}%")
                        ->orWhere('headline', 'LIKE', "%{$search}%");
                })->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
            $totalFiltered = CustomAdVideos::where('ad_id', $request->adId)
                ->where(function ($query) use ($search) {
                    $query->where('description', 'LIKE', "%{$search}%")
                        ->orWhere('headline', 'LIKE', "%{$search}%");
                })->count();
        }
        $data = array();
        foreach ($result as $item) {

            $vidURL = GlobalFunction::createMediaUrl($item->video);

            $view = '<a data-description="' . $item->description . '" data-headline="' . $item->headline . '" class="btn btn-info text-white pointer show-vid" rel="' . $vidURL . '">View</a>';

            $delete = '<a href=""class=" btn btn-danger text-white delete-vid " rel=' . $item->id . ' >Delete</a>';

            $edit = '<a data-video="' . $vidURL . '" data-show-time="' . $item->show_time . '" data-description="' . $item->description . '" data-type="' . $item->type . '" data-headline="' . $item->headline . '" class=" btn btn-info text-white edit-vid mr-2" rel=' . $item->id . ' >Edit</a>';

            // Type
            $type = "";
            if ($item->type == 0) {
                // Must Watch
                $type = '<span  class="badge bg-warning text-white badge-shadow ">MUST WATCH</span>';
            }
            if ($item->type == 1) {
                // Skippable
                $type = '<span  class="badge bg-success text-white badge-shadow ">SKIPPABLE</span>';
            }

            $action = $edit . $delete;

            $data[] = array(
                $view,
                $item->headline,
                $item->description,
                $type,
                Carbon::createFromFormat('Y-m-d H:i:s', $item->created_at)->format('d M, Y'),
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

    function editAd(Request $request)
    {
        $ad = CustomAds::find($request->id);
        $ad->title = $request->title;
        $ad->brand_name = $request->brand_name;
        $ad->is_android = $request->is_android;
        $ad->android_link = $request->android_link;
        $ad->is_ios = $request->is_ios;
        $ad->ios_link = $request->ios_link;
        $ad->button_text = $request->button_text;
        $ad->end_date = $request->end_date;
        $ad->start_date = $request->start_date;
        if ($request->has('brand_logo')) {
            $ad->brand_logo = GlobalFunction::saveFileAndGivePath($request->file('brand_logo'));
        }
        $ad->save();
        return response()->json(['status' => true]);
    }

    function editCustomAd($id)
    {
        $ad = CustomAds::find($id);

        return view('admin.settings.editCustomAd')->with('customAd', $ad);
    }
    function viewCustomAd($id)
    {
        $ad = CustomAds::find($id);
        if ($ad->clicks != 0) {
            $ctr = ($ad->clicks / $ad->views) * 100;
            $ad->ctr = $ctr . "%";
        } else {
            $ad->ctr = "0%";
        }
        return view('admin.settings.viewCustomAd')->with('customAd', $ad);
    }

    function deleteCustomAd(Request $request)
    {
        CustomAds::where('id', $request->id)->delete();
        CustomAdImages::where('ad_id', $request->id)->delete();
        CustomAdVideos::where('ad_id', $request->id)->delete();
        $response['success'] = 1;
        echo json_encode($response);
    }

    function createNewAd(Request $request)
    {
        $ad = new CustomAds();
        $ad->campaign_number = GlobalFunction::generateCodeNumber();
        $ad->title = $request->title;
        $ad->brand_name = $request->brand_name;
        $ad->is_android = $request->is_android;
        $ad->android_link = $request->android_link;
        $ad->is_ios = $request->is_ios;
        $ad->ios_link = $request->ios_link;
        $ad->button_text = $request->button_text;
        $ad->end_date = $request->end_date;
        $ad->start_date = $request->start_date;
        $ad->brand_logo = GlobalFunction::saveFileAndGivePath($request->file('brand_logo'));

        $ad->save();
        return response()->json(['status' => true]);
    }

    function createCustomAd()
    {
        return view('admin.settings.createCustomAd');
    }

    function changeAdStatus($id, $status)
    {
        $imgCount = CustomAdImages::where('ad_id', $id)->count();
        $videoCount = CustomAdVideos::where('ad_id', $id)->count();

        if ($imgCount != 0 || $videoCount != 0) {
            $ad = CustomAds::find($id);
            $ad->status = $status;
            $ad->save();
            return response()->json(['status' => true]);
        }
        return response()->json(['status' => false]);
    }

    function showCustomAdsList(Request $request)
    {
        $columns = array(
            0 => 'id',
        );

        $totalData = CustomAds::count();

        $totalFiltered = $totalData;

        $limit = $request->input('length');
        $start = $request->input('start');
        $order = $columns[$request->input('order.0.column')];
        $dir = $request->input('order.0.dir');

        if (empty($request->input('search.value'))) {
            $Data = CustomAds::offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();
        } else {
            $search = $request->input('search.value');

            $Data =  CustomAds::where('campaign_number', 'LIKE', "%{$search}%")
                ->orWhere('brand_name', 'LIKE', "%{$search}%")
                ->orWhere('title', 'LIKE', "%{$search}%")
                ->offset($start)
                ->limit($limit)
                ->orderBy($order, $dir)
                ->get();

            $totalData  = $totalFiltered = CustomAds::where('campaign_number', 'LIKE', "%{$search}%")
                ->orWhere('brand_name', 'LIKE', "%{$search}%")
                ->orWhere('title', 'LIKE', "%{$search}%")
                ->count();
        }

        $data = array();
        if (!empty($Data)) {
            foreach ($Data as $item) {

                // brand Logo
                if ($item->brand_logo) {
                    $brand_logo = '<img class="img-lg rounded" src="' . url(env('DEFAULT_IMAGE_URL') . $item->brand_logo) . '" width="60" height="60"/>';
                } else {
                    $brand_logo = '<img class="img-lg rounded" src="https://placehold.jp/150x150.png" width="60" height="60"/>';
                }

                // Platform
                $android = "";
                if ($item->is_android == 1) {
                    $android = '<a href="' . $item->android_link . '" target="_blank" ><span  class="badge bg-success text-white badge-shadow "><i class="i-cl-3 fa fa-link col-white font-12 pointer mr-1"></i>' . __("ANDROID") . '</span></a>';
                }
                $ios = "";
                if ($item->is_ios == 1) {
                    $ios = '<a class="ml-1" href="' . $item->ios_link . '" target="_blank" ><span  class="badge bg-dark text-white badge-shadow "><i class="i-cl-3 fa fa-link col-white font-12 pointer mr-1"></i>' . __("iOS") . '</span></a>';
                }
                $platform = $android . $ios;


                // On/OffStatus
                $status = "";
                if ($item->status == 0) {
                    $status = '  <label class="switch ">
                                <input rel=' . $item->id . '  type="checkbox" class="ad_status">
                                <span class="slider round"></span>
                            </label>';
                } else {
                    $status =
                        '  <label class="switch ">
                                <input rel=' . $item->id . '  type="checkbox" class="ad_status" checked>
                                <span class="slider round"></span>
                            </label>';
                }

                $editAd =  route('ads/edit/', $item->id);
                $viewAd = route('ads/view/', $item->id);
                // Action
                $view = '<a href="' . $viewAd . '" class="viewCustomAd" data-id="' . $item->id . '" ><i class="i-cl-3 fa fa-eye col-blue font-20 pointer p-l-5 p-r-5"></i></a>';
                $edit = '<a href="' . $editAd . '" class="editCustomAd" data-id="' . $item->id . '" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a>';
                $delete = '<a class="delete deleteCustomAd" data-id="' . $item->id . '" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>';
                $action = $view . $edit . $delete;

                $data[] = array(
                    $brand_logo,
                    $item->campaign_number,
                    $item->title,
                    $item->brand_name,
                    $platform,
                    $item->views,
                    $item->clicks,
                    $item->start_date,
                    $item->end_date,
                    $status,
                    $action
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
