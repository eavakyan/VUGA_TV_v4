<?php

namespace App\Http\Controllers;

use App\Actor;
use App\Content;
use App\ContentCast;
use App\GlobalFunction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class ActorController extends Controller
{
    public function fetchActorFromTMDB()
    { 
        return view('fetchActorFromTMDB');
    }

    function actors()
    {
        $TMDBAPI = ['TMDB_API_KEY' => env('TMDB_API_KEY')];
        return view('actors', compact('TMDBAPI'));
    }

    public function actorsList(Request $request)
    {
        $query = Actor::query();
        $totalData = $query->count();

        $columns = ['id'];
        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where(function ($q) use ($searchValue) {
                $q->where('fullname', 'LIKE', "%{$searchValue}%");
            });
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $image = "<div class='d-flex align-items-center'>
                    <img data-fancybox src='{$item->profile_image}' class='object-fit-cover border-radius img-border' width='60px' height='60px'>
                    <span class='ms-3'>{$item->fullname}</span>
                </div>";

            $edit = "<a rel='{$item->id}'
                    data-fullname='{$item->fullname}' 
                    data-profile_image='{$item->profile_image}' 
                    data-dob='{$item->dob}' 
                    data-bio='{$item->bio}' 
                    class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";

            $action = "<div class='text-end action'>{$edit}{$delete}</div>";

            return [
                $image,
                $item->dob,
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

    public function addNewActor(Request $request)
    {
        $actor = new Actor();
        $actor->fullname = $request->fullname;
        $actor->dob = $request->dob;
        $actor->bio = $request->bio;

        if ($request->hasFile('profile_image')) {
            $actorProfile = $request->file('profile_image');
            $profileImagePath = GlobalFunction::saveFileAndGivePath($actorProfile);
           
        } elseif ($request->has('profile_path_url')) {
            $profileImagePath = GlobalFunction::saveImageFromUrl($request->profile_path_url);
        } else {
            $profileImagePath = null;
        }
        $actor->profile_image = $profileImagePath;
        
        $actor->save();

        return response()->json([
            'status' => true,
            'message' => 'Actor Added Successfully',
            'data' => $actor,
        ]);
    }

    public function updateActor(Request $request)
    {
        $actor = Actor::where('id', $request->actor_id)->first();
        if ($actor == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $actor->fullname = $request->fullname;
        $actor->dob = $request->dob;
        $actor->bio = $request->bio;

        if ($request->hasFile('profile_image')) {
            GlobalFunction::deleteFile($actor->profile_image);
            
            $actorProfile = $request->file('profile_image');
            $profileImagePath = GlobalFunction::saveFileAndGivePath($actorProfile);
            $actor->profile_image = $profileImagePath;
        }

        $actor->save();

        return response()->json([
            'status' => true,
            'message' => 'Actor Updated Successfully',
            'data' => $actor,
        ]);
    }
    
    public function deleteActor(Request $request)
    {
        $actor = Actor::where('id', $request->actor_id)->first();
        if ($actor == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        ContentCast::where('actor_id', $request->actor_id)->delete();
        GlobalFunction::deleteFile($actor->profile_image);

        $actor->delete();

        return response()->json([
            'status' => true,
            'message' => 'Actor Deleted Successfully',
            'data' => $actor,
        ]);
    }

    function fetchActorDetails(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'actor_id' => 'required',
        ]);

        if ($validator->fails()) {
            $msg = $validator->errors()->first();
            return response()->json(['status' => false, 'message' => $msg]);
        }

        $actor = Actor::where('id', $request->actor_id)->first();
        if (!$actor) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $castContentIds = ContentCast::where('actor_id', $request->actor_id)->pluck('content_id');
        $actorContent = Content::whereIn('id', $castContentIds)->get();

        $actor->actorContent = $actorContent;

        return response()->json([
            'status' => true,
            'message' => 'Actor Details Fetched Successfully',
            'data' => $actor,
        ]);

    }

}
