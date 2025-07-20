<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Actor;
use Illuminate\Http\Request;

class ActorController extends Controller
{
    public function viewListActor()
    {
        return view('admin.actor.list');
    }

    public function showActorList(Request $request)
    {
        $query = Actor::query();
        
        if ($request->has('search') && !empty($request->search)) {
            $query->where('name', 'LIKE', "%{$request->search}%");
        }

        $actors = $query->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $actors->items(),
            'recordsTotal' => $actors->total(),
            'recordsFiltered' => $actors->total()
        ]);
    }

    public function addUpdateActor(Request $request)
    {
        if ($request->id) {
            $actor = Actor::find($request->id);
        } else {
            $actor = new Actor();
        }
        
        $actor->name = $request->name;
        $actor->save();

        return response()->json([
            'status' => true,
            'message' => 'Actor saved successfully'
        ]);
    }

    public function deleteActor(Request $request)
    {
        $actor = Actor::find($request->id);
        if ($actor) {
            $actor->delete();
            return response()->json(['status' => true, 'message' => 'Actor deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Actor not found']);
    }

    public function CheckExistActor(Request $request)
    {
        $exists = Actor::where('name', $request->name)
                      ->when($request->id, function($q) use ($request) {
                          $q->where('id', '!=', $request->id);
                      })
                      ->exists();
        
        return response()->json(['exists' => $exists]);
    }
} 