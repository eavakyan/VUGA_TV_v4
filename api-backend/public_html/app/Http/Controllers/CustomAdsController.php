<?php

namespace App\Http\Controllers;

use App\Constants;
use App\CustomAd;
use App\CustomAdSource;
use App\GlobalFunction;
use App\GlobalSettings;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class CustomAdsController extends Controller
{
    function customAds()
    {
        return view('customAds');
    }

    public function fetchCustomAdList(Request $request)
    {
        $columns = ['id'];
        $query = CustomAd::query();
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where('title', 'LIKE', "%{$searchValue}%")
                ->orWhere('brand_name', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
                        ->offset($start)
                        ->limit($limit)
                        ->get();

        $data = $result->map(function ($item) {
            $imageHtml = "<div class='d-flex align-items-center'>
                        <img src='{$item->brand_logo}' data-fancybox class='object-fit-cover border-radius outline' width='60px' height='60px'>
                        <span class='ms-3'>{$item->title}</span>
                      </div>";

            $platformLinks = '';
            if ($item->is_android == 1) {
                $platformLinks .= "<a href='{$item->android_link}' target='_blank'><span class='badge bg-success text-white badge-shadow me-2'><svg viewBox='0 0 24 24' width='12' height='12' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round' class='css-i6dzq1 me-1'><path d='M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71'></path><path d='M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71'></path></svg>ANDROID</span></a>";
            } 
            if ($item->is_ios == 1) {
                $platformLinks .= "<a href='{$item->ios_link}' class='ml-1' target='_blank'><span class='badge bg-dark text-white badge-shadow'><svg viewBox='0 0 24 24' width='12' height='12' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round' class='css-i6dzq1 me-1'><path d='M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71'></path><path d='M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71'></path></svg>iOS</span></a>";
            }

            if ($item->status == Constants::On) {
                $status = '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none customAdOff" checked rel="' . $item->id . '" value="' . $item->status . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
                </div>';
            } else {
                $status = '<div class="checkbox-slider d-flex align-items-center">
                    <label>
                        <input type="checkbox" class="d-none customAdOn" rel="' . $item->id . '" value="' . $item->status . '" >
                        <span class="toggle_background">
                            <div class="circle-icon"></div>
                            <div class="vertical_line"></div>
                        </span>
                    </label>
                </div>';
            }

            $customAdDetail = '<a href="customAdDetailView/' . $item->id . '" class="btn btn-info me-2 shadow-none text-white" style="white-space: nowrap;">' . __('detail') . '</a>';

            $edit = "<a rel='{$item->id}'
                        data-brand_logo='{$item->brand_logo}' 
                        data-title='{$item->title}' 
                        data-brand_name='{$item->brand_name}' 
                        data-button_text='{$item->button_text}' 
                        data-is_android='{$item->is_android}' 
                        data-android_link='{$item->android_link}' 
                        data-is_ios='{$item->is_ios}' 
                        data-ios_link='{$item->ios_link}' 
                        data-start_date='{$item->start_date}' 
                        data-end_date='{$item->end_date}' 
                        data-status='{$item->status}' 
                        class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";

            $actionHtml = "<div class='text-end action'> {$customAdDetail} {$edit} {$delete}</div>";

            $startDate = date('d-m-Y', strtotime($item->start_date));
            $endDate = date('d-m-Y', strtotime($item->end_date));

            return [
                $imageHtml,
                $item->brand_name,
                $platformLinks,
                $item->views,
                $item->clicks,
                $startDate,
                $endDate,
                $status,
                $actionHtml,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data
        ];

        return response()->json($json_data);
    }

    function customAdOn(Request $request)
    {
        $customAd = CustomAd::where('id', $request->ad_id)->with('sources')->first();

        if ($customAd->sources->isEmpty()) {
            return response()->json([
                'status' => false,
                'message' => 'Custom Ad cannot be turned on without any sources.',
            ]);
        }

        $customAd->status = Constants::On;
        $customAd->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad On',
        ]);    
    }

    function customAdOff(Request $request)
    {
        $customAd = CustomAd::where('id', $request->ad_id)->first();
        $customAd->status = Constants::Off;
        $customAd->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Off',
        ]);    
    }

    function addCustomAd(Request $request)
    {
        $customAd = new CustomAd();
        $customAd->title = $request->title;
        $customAd->brand_name = $request->brand_name;
        
        if ($request->has('brand_logo')) {
            $customAd->brand_logo = GlobalFunction::saveFileAndGivePath($request->file('brand_logo'));
        }
        $customAd->button_text = $request->button_text;
        $customAd->start_date = $request->start_date;
        $customAd->end_date = $request->end_date;
        
        if ($request->has('android_link')) {
            $customAd->is_android = $request->is_android;
            $customAd->android_link = $request->android_link;
        }
        if ($request->has('ios_link')) {
            $customAd->is_ios = $request->is_ios;
            $customAd->ios_link = $request->ios_link;
        }
        $customAd->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Created Successfully',
        ]);    
    }

    function updateCustomAd(Request $request)
    {
        $customAd = CustomAd::find($request->custom_ad_id);

        if ($customAd == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        

        $customAd->title = $request->title;
        $customAd->brand_name = $request->brand_name;

        if ($request->has('brand_logo')) {
            GlobalFunction::deleteFile($customAd->brand_logo);

            $customAd->brand_logo = GlobalFunction::saveFileAndGivePath($request->file('brand_logo'));
        }
        $customAd->button_text = $request->button_text;
        $customAd->start_date = $request->start_date;
        $customAd->end_date = $request->end_date;

        if ($request->has('android_link')) {
            $customAd->is_android = $request->is_android;
            $customAd->android_link = $request->android_link;
        } else {
            $customAd->is_android = 0;
            $customAd->android_link = null;
        }

        if ($request->has('ios_link')) {
            $customAd->is_ios = $request->is_ios;
            $customAd->ios_link = $request->ios_link;
        } else {
            $customAd->is_ios = 0;
            $customAd->ios_link = null;
        }
        
        $customAd->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Updated Successfully',
        ]);    
    }

    function deleteCustomAd(Request $request)
    {
        $customAd = CustomAd::where('id', $request->custom_ad_id)->first();
        if ($customAd == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        GlobalFunction::deleteFile($customAd->brand_logo);
        
        $customAdSources = CustomAdSource::where('custom_ad_id', $customAd->id)->get();
        foreach ($customAdSources as $customAdSource) {
            GlobalFunction::deleteFile($customAdSource->content);
            $customAdSource->delete();
        }

        $customAd->delete();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Deleted Successfully',
            'data' => $customAd,
        ]);
    }

    function customAdDetailView(Request $request)
    {
        $customAd = CustomAd::where('id', $request->id)->first();
        if ($customAd == null) {
            return response()->json([
                'status' => false,
                'message' => 'Custom Ad Not Found',
            ]);
        }

        return view('customAdDetailView', [
            'customAd' => $customAd,
        ]);
    }

    function fetchCustomAdImageSourceList(Request $request)
    {
        $columns = ['id'];
        $query = CustomAdSource::where('custom_ad_id', $request->custom_ad_id)->where('type', 0);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');


        if (!empty($searchValue)) {
            $query->where('headline', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {

            $imageHtml = "<div class='d-flex align-items-center'>
                        <img src='{$item->content}' data-fancybox class='object-fit-cover border-radius' width='60px' height='60px'>
                        <span class='ms-3'>{$item->headline}</span>
                      </div>";
            $description = "<span class='itemDescription'>{$item->description}</span>";

            $edit = "<a rel='{$item->id}'
                        data-content='{$item->content}' 
                        data-headline='{$item->headline}' 
                        data-description='{$item->description}' 
                        data-show_time='{$item->show_time}' 
                        class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";

            $actionHtml = "<div class='text-end action'> {$edit} {$delete}</div>";

            return [
                $imageHtml,
                $description,
                $item->show_time,
                $actionHtml,
            ];
        });

        // Prepare the JSON response
        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data
        ];

        // Return the response as JSON
        return response()->json($json_data);
    }

    function addCustomAdSourceImage(Request $request)
    {
        $customAdSource = new CustomAdSource();
        $customAdSource->custom_ad_id = $request->custom_ad_id;
        $customAdSource->type = Constants::CustomAdSourceTypeImage;
        
        if ($request->has('content')) {
            $customAdSource->content = GlobalFunction::saveFileAndGivePath($request->file('content'));
        }
        $customAdSource->headline = $request->headline;
        $customAdSource->description = $request->description;
        $customAdSource->show_time = $request->show_time;
        $customAdSource->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Source Added Successfully',
        ]);
    }

    function updateCustomAdSource(Request $request)
    {
        $customAdSource = CustomAdSource::find($request->custom_ad_source_id);

        if ($customAdSource == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        if ($request->has('content')) {
            GlobalFunction::deleteFile($customAdSource->content);
            $customAdSource->content = GlobalFunction::saveFileAndGivePath($request->file('content'));
        }
        
        $customAdSource->headline = $request->headline;
        $customAdSource->description = $request->description;
        
        if ($request->has('show_time')) {
            $customAdSource->show_time = $request->show_time;
        }
        if ($request->has('is_skippable')) {
            $customAdSource->is_skippable = $request->is_skippable;
        }
        
        $customAdSource->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Source Updated Successfully',
        ]);      
    }

    function deleteCustomAdSource(Request $request)
    {
        $customAdSource = CustomAdSource::where('id', $request->custom_ad_source_id)->first();
        if ($customAdSource == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        GlobalFunction::deleteFile($customAdSource->content);
        $customAdSource->delete();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Source Deleted Successfully',
            'data' => $customAdSource,
        ]);
    }

    function fetchCustomAdVideoSourceList(Request $request)
    {
        $columns = ['id'];
        $query = CustomAdSource::where('custom_ad_id', $request->custom_ad_id)->where('type', Constants::CustomAdSourceTypeVideo);
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');


        if (!empty($searchValue)) {
            $query->where('headline', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $videoHtml = "<div class='d-flex align-items-center'>
                        <a href='javascript:;' rel='{$item->id}' data-source_url='{$item->content}' class='btn-primary text-white px-3 py-1 border-radius source_file_video'>" . __('preview') . "</a>
                        <span class='ms-3'>{$item->headline}</span>
                        </div>";
            $description = "<span class='itemDescription'>{$item->description}</span>";

            $isSkippableBadge = $item->is_skippable == 0
            ? "<span class='badge bg-danger px-3'>" . __('mustWatch') . "</span>"
                : "<span class='badge bg-success px-3'>" . __('skippable') . "</span>";

            $edit = "<a rel='{$item->id}'
                        data-content='{$item->content}' 
                        data-headline='{$item->headline}' 
                        data-description='{$item->description}' 
                        data-is_skippable='{$item->is_skippable}' 
                        class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";

            $actionHtml = "<div class='text-end action'> {$edit} {$delete}</div>";

            return [
                $videoHtml,
                $description,
                $isSkippableBadge,
                $actionHtml,
            ];
        });

        // Prepare the JSON response
        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data
        ];

        // Return the response as JSON
        return response()->json($json_data);
    }

    function addCustomAdSourceVideo(Request $request)
    {
        $customAdSource = new CustomAdSource();
        $customAdSource->custom_ad_id = $request->custom_ad_id;
        $customAdSource->type = Constants::CustomAdSourceTypeVideo;

        if ($request->has('content')) {
            $customAdSource->content = GlobalFunction::saveFileAndGivePath($request->file('content'));
        }
        $customAdSource->headline = $request->headline;
        $customAdSource->description = $request->description;
        $customAdSource->is_skippable = $request->is_skippable;
        $customAdSource->save();

        return response()->json([
            'status' => true,
            'message' => 'Custom Ad Source Added Successfully',
        ]);
    }

    function fetchCustomAds(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'is_android' => 'nullable|boolean',
            'is_ios' => 'nullable|boolean',
        ]);

        $validator->after(function ($validator) use ($request) {
            if (!$request->is_android && !$request->is_ios) {
                $validator->errors()->add('is_android', 'At least one of Android or iOS must be selected.');
                $validator->errors()->add('is_ios', 'At least one of Android or iOS must be selected.');
            }
        });

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $setting = GlobalSettings::first();

        $ads = collect();

        if ($request->is_android == 1) {
            if ($setting->is_custom_android == 1) {
                $androidAds = CustomAd::where('is_android', 1)
                                        ->where('status', 1)
                                        ->has('sources')
                                        ->with('sources')
                                        ->inRandomOrder()
                                        ->limit(10)
                                        ->get();
                $ads = $ads->merge($androidAds);
            } else {
                return response()->json([
                    'status' => false,
                    'message' => 'Custom Ad is disabled for Android by Admin'
                ]);
            }
        }

        if ($request->is_ios == 1) {
            if ($setting->is_custom_ios == 1) {
                $iosAds = CustomAd::where('is_ios', 1)
                                    ->where('status', 1)
                                    ->has('sources')
                                    ->with('sources')
                                    ->inRandomOrder()
                                    ->limit(10)
                                    ->get();
                $ads = $ads->merge($iosAds);
            } else {
                return response()->json([
                    'status' => false,
                    'message' => 'Custom Ad is disabled for iOS by Admin'
                ]);
            }
        }

        return response()->json([
            'status' => true,
            'message' => 'Fetch Custom Ad Successfully',
            'data' => $ads
        ]);
    }

    function increaseAdMetric(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'custom_ad_id' => 'required',
            'metric' => 'required|in:click,view',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $customAd = CustomAd::where('id', $request->custom_ad_id)->first();
        if ($customAd == null) {
            return response()->json([
                'status' => false,
                'message' => "Custom Ad doesn't exist!"
            ]);
        }

        if ($request->metric == 'click') {
            $customAd->clicks += 1;
            $message = 'Ad Click Increased Successfully';
        } else if ($request->metric == 'view') {
            $customAd->views += 1;
            $message = 'Ad Views Increased Successfully';
        }

        $customAd->save();

        return response()->json([
            'status' => true,
            'message' => $message,
            'data' => $customAd,
        ]);
    }
    
}
