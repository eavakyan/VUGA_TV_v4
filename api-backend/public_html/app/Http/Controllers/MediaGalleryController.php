<?php

namespace App\Http\Controllers;

use App\Constants;
use App\ContentSource;
use App\EpisodeSource;
use App\GlobalFunction;
use App\MediaGallery;
use Illuminate\Http\Request;

class MediaGalleryController extends Controller
{
    public function mediaGallery()
    {
        return view('mediaGallery');
    }

    public function mediaGalleryList(Request $request)
    {
        $query = MediaGallery::query();
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

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
 
            $source = '<a href="javascript:;" 
                            rel="' . $item->id . '"  
                            data-source_url="' . $item->file . '" 
                            class="me-2 btn btn-primary px-4 text-white source_file_video"> Video Preview </a>';
            $edit = "<a rel='{$item->id}' data-title='{$item->title}' data-file='{$item->file}' class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";
            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";
            $action = "<div class='text-end action'>{$edit}{$delete}</div>";
            return [
                $source,
                $item->title,
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

    public function addMedia(Request $request)
    {

        $file = $request->file('file');
        $path = GlobalFunction::saveFileAndGivePath($file);

        $media = new MediaGallery();
        $media->title = $request->title;
        $media->file = $path;
        $media->save();

        return response()->json([
            'status' => true,
            'message' => 'Media Added Successfully',
        ]);
    }


    public function updateMedia(Request $request)
    {
        $media = MediaGallery::where('id', $request->media_id)->first();
        if (!$media) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        if ($request->has('title')) {
            $media->title = $request->title;
        }

        if ($request->hasFile('file')) {
            GlobalFunction::deleteFile($media->file);
            $file = $request->file('file');
            $media->file = GlobalFunction::saveFileAndGivePath($file);
        }

        $media->save();

        return response()->json([
            'status' => true,
            'message' => 'Media Updated Successfully',
            'data' => $media,
        ]);
    }

    public function deleteMedia(Request $request)
    {
        $media = MediaGallery::where('id', $request->media_id)->first();

        if (!$media) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        ContentSource::where('type', Constants::FileType)
                     ->where('source', $request->media_id)
                     ->delete();
        
        EpisodeSource::where('type', Constants::FileType)
                        ->where('source', $request->media_id)
                        ->delete();

        GlobalFunction::deleteFile($media->file);
        $media->delete();

        return response()->json([
            'status' => true,
            'message' => 'Media Deleted Successfully',
        ]);

    }

}
