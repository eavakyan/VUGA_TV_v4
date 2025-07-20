<?php

namespace App\Http\Controllers;

use App\Constants;
use App\GlobalFunction;
use App\TVCategory;
use App\TVChannel;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class TVController extends Controller
{
    function fetchLiveTVPageData()
    {
        $tvCategories = TVCategory::orderBy('id', 'DESC')->get();

        foreach ($tvCategories as $category) {
            $channels = TVChannel::whereRaw('FIND_IN_SET(?, category_ids)', [$category->id])->limit(env('LIVE_TV_DATA_COUNT'))->orderBy('id', 'DESC')->get();
            $category->channels = $channels;
        }

        return response()->json([
            'status' => true,
            'message' => 'Fetched TV Categories and Channels successfully',
            'data' => $tvCategories
        ]);
    }

    function fetchTVChannelByCategory(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_category_id' => 'required',
            'start' => 'required',
            'limit' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $tvCategory = TVCategory::where('id', $request->tv_category_id)->first();
        if ($tvCategory == null) {
            return response()->json([
                'status' => false,
                'message' => 'TV Category Not Found'
            ]);
        }

        $start = $request->start;
        $limit = $request->limit;


        $channels = TVChannel::whereRaw('FIND_IN_SET(?, category_ids)', [$request->tv_category_id])
                            ->orderBy('id', 'DESC')
                            ->skip($start)
                            ->take($limit)
                            ->get();
        $tvCategory->channels = $channels;
        
        return response()->json([
            'status' => true,
            'message' => 'TV Channels By Category',
            'data' => $tvCategory
        ]);
    }

    function searchTVChannel(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'keyword' => 'required',
            'start' => 'required',
            'limit' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $start = $request->start;
        $limit = $request->limit;

        $channels = TVChannel::where('title', 'like', '%' . $request->keyword . '%')
                                ->orderBy('id', 'DESC')
                                ->skip($start)
                                ->take($limit)
                                ->get();

        return response()->json([
            'status' => true,
            'message' => 'Search Result Successfully',
            'data' => $channels
        ]);
    }

    function increaseTVChannelView(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'channel_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $tvChannel = TVChannel::where('id', $request->channel_id)->first();
        $tvChannel->total_view += 1;
        $tvChannel->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase TV Channel View Successfully',
            'data' => $tvChannel
        ]);

    }

    function increaseTVChannelShare(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'channel_id' => 'required',
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $tvChannel = TVChannel::where('id', $request->channel_id)->first();
        $tvChannel->total_share += 1;
        $tvChannel->save();

        return response()->json([
            'status' => true,
            'message' => 'Increase TV Channel View Successfully',
            'data' => $tvChannel
        ]);

    }

    // Web
    public function liveTvCategories()
    {
        return view('liveTvCategories');
        
    }

    public function fetchTvCategoryList(Request $request)
    {
        $query = TVCategory::query();
        $totalData = $query->count();

        $columns = ['id'];
        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');

        $searchValue = $request->input('search.value');
        if (!empty($searchValue)) {
            $query->where('title', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->offset($start)
            ->limit($limit)
            ->orderBy($orderColumn, $orderDir)
            ->get();

        $data = $result->map(function ($item) {
            $imageUrl = $item->image ? $item->image : './assets/img/placeholder-image.png';
            $image = "<div class='d-flex align-items-center'>
                    <img src='{$imageUrl}' data-fancybox class='object-fit-cover border-radius bg-white' width='60px' height='60px'>
                    <span class='ms-3'>{$item->title}</span>
                  </div>";

            $edit = "<a rel='{$item->id}' data-title='{$item->title}' data-image='{$imageUrl}' class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . '</a>';

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . '</a>';

            $action = "<div class='text-end action'>{$edit}{$delete}</div>";

            return [
                $image,
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

    public function addTvCategory(Request $request)
    {
        $tvCategory = new TVCategory();
        $tvCategory->title = $request->title;

        if ($request->hasFile('image')) {
            $path = GlobalFunction::saveFileAndGivePath($request->file('image'));
            $tvCategory->image = $path;
        }
        $tvCategory->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Category Added Successfully',
            'data' => $tvCategory,
        ]);
    }

    public function updateTvCategory(Request $request)
    {
        $tvCategory = TVCategory::where('id', $request->tv_category_id)->first();

        if (!$tvCategory) {
            return response()->json([
                'status' => false,
                'message' => 'Category Not Found',
            ]);
        }

        $tvCategory->title = $request->title;

        if ($request->hasFile('image')) {
            if ($tvCategory->image) {
                GlobalFunction::deleteFile($tvCategory->image);
            }
            $path = GlobalFunction::saveFileAndGivePath($request->file('image'));
            $tvCategory->image = $path;
        }

        $tvCategory->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Category Updated Successfully',
            'data' => $tvCategory,
        ]);
    }
 
    public function deleteTvCategory(Request $request)
    {

        $tvCategory = TVCategory::where('id', $request->tv_category_id)->first();
        if (!$tvCategory) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $tvChannels = TVChannel::where('category_ids', 'LIKE', '%' . $request->tv_category_id . '%')->get();

        foreach ($tvChannels as $tvChannel) {
            $categories = explode(',', $tvChannel->category_ids);
            $categories = array_filter($categories, function ($value) use ($request) {
                return $value != $request->tv_category_id;
            });
            $tvChannel->category_ids = implode(',', $categories);
            $tvChannel->save();
        }

        GlobalFunction::deleteFile($tvCategory->image);
        $tvCategory->delete();

        return response()->json([
            'status' => true,
            'message' => 'TV Category Deleted Successfully',
            'data' => $tvCategory,
        ]);
    }

    public function liveTvChannels()
    {
        $tvCategories = TVCategory::get();

        return view('liveTvChannels', [
            'tvCategories' => $tvCategories,
        ]);
    }

    public function fetchTvChannelList(Request $request)
    {
        $columns = ['id'];
        $query = TVChannel::query();

        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue) {
                $q->where('title', 'LIKE', "%{$searchValue}%");
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $imageHtml = "<div class='d-flex align-items-center'>
                        <img src='{$item->thumbnail}' data-fancybox class='object-fit-cover border-radius' width='60px' height='60px'>
                        <span class='ms-3'>{$item->title}</span>
                      </div>";


            $categoryIds = explode(',', $item->category_ids);
            $categories = TVCategory::whereIn('id', $categoryIds)->get();
            $categoryTitles = $categories->pluck('title')->implode(', ');

            if ($item->type == Constants::Youtube) {
                $sourceUrl = 'https://youtu.be/' . $item->source;
                $source = "<a href='{$sourceUrl}' target='_blank' class='sourceUrlLink'> <svg viewBox='0 0 24 24' width='24' height='24' stroke='currentColor' stroke-width='2' fill='none' stroke-linecap='round' stroke-linejoin='round' class='css-i6dzq1'><path d='M22.54 6.42a2.78 2.78 0 0 0-1.94-2C18.88 4 12 4 12 4s-6.88 0-8.6.46a2.78 2.78 0 0 0-1.94 2A29 29 0 0 0 1 11.75a29 29 0 0 0 .46 5.33A2.78 2.78 0 0 0 3.4 19c1.72.46 8.6.46 8.6.46s6.88 0 8.6-.46a2.78 2.78 0 0 0 1.94-2 29 29 0 0 0 .46-5.25 29 29 0 0 0-.46-5.33z' stroke='#FF0000'></path><polygon points='9.75 15.02 15.5 11.75 9.75 8.48 9.75 15.02' stroke='#FF0000' ></polygon></svg> </a>";
            } else {
                $source = "<a href='{$item->source}' data-type='{$item->type}' data-source='{$item->source}' class='m3u8_Url_Link'>" . __('preview') . " </a>";
            }
             

            $edit = "<a rel='{$item->id}'
                    data-title='{$item->title}' 
                    data-thumbnail='{$item->thumbnail}' 
                    data-access_type='{$item->access_type}' 
                    data-category_ids='{$item->category_ids}' 
                    data-type='{$item->type}' 
                    data-source='{$item->source}' 
                    class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . '</a>';

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . '</a>';

            $actionHtml = "<div class='text-end action'>{$edit} {$delete}</div>";

            $type = match ($item->type) {
                1 => 'Youtube Id',
                default => 'M3u8 Url',
            };

            return [
                $imageHtml,
                $categoryTitles,
                $type,
                $source,
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
    
    public function addTvChannel(Request $request)
    {
        $tvChannel = new TVChannel();
        $tvChannel->title = $request->title;
        
        if ($request->hasFile('thumbnail')) {
            $path = GlobalFunction::saveFileAndGivePath($request->file('thumbnail'));
            $tvChannel->thumbnail = $path;
        }
        $tvChannel->access_type = $request->access_type;
        $tvChannel->category_ids = implode(',', $request->category_ids);
        $tvChannel->type = $request->type;
        $tvChannel->source = $request->source;
        $tvChannel->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Channel Added Successfully',
            'data' => $tvChannel,
        ]);
    }

    public function updateTvChannel(Request $request)
    {
        $tvChannel = TVChannel::where('id', $request->tv_channel_id)->first();

        if (!$tvChannel) {
            return response()->json([
                'status' => false,
                'message' => 'TV Channel Not Found',
            ]);
        }

        $tvChannel->title = $request->title;

        if ($request->hasFile('thumbnail')) {
            if ($tvChannel->thumbnail) {
                GlobalFunction::deleteFile($tvChannel->thumbnail);
            }
            $path = GlobalFunction::saveFileAndGivePath($request->file('thumbnail'));
            $tvChannel->thumbnail = $path;
        }

        $tvChannel->access_type = $request->access_type;
        $tvChannel->category_ids = implode(',', $request->category_ids);
        $tvChannel->type = $request->type;
        $tvChannel->source = $request->source;

        $tvChannel->save();

        return response()->json([
            'status' => true,
            'message' => 'TV Channel Updated Successfully',
            'data' => $tvChannel,
        ]);
    }

    public function deleteTvChannel(Request $request)
    {
        $tvChannel = TVChannel::where('id', $request->tv_channel_id)->first();

        if (!$tvChannel) {
            return response()->json([
                'status' => false,
                'message' => 'TV Channel Not Found',
            ]);
        }

        GlobalFunction::deleteFile($tvChannel->thumbnail);

        $tvChannel->delete();

        return response()->json([
            'status' => true,
            'message' => 'TV Channel Deleted Successfully',
            'data' => $tvChannel,
        ]);
    }

}
