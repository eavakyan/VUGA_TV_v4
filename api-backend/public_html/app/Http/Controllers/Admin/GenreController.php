<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Genre;
use Illuminate\Http\Request;

class GenreController extends Controller
{
    public function viewListGenre()
    {
        return view('admin.genre.list');
    }

    public function showGenreList(Request $request)
    {
        $query = Genre::query();
        
        if ($request->has('search') && !empty($request->search)) {
            $query->where('title', 'LIKE', "%{$request->search}%");
        }

        $genres = $query->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $genres->items(),
            'recordsTotal' => $genres->total(),
            'recordsFiltered' => $genres->total()
        ]);
    }

    public function addUpdateGenre(Request $request)
    {
        if ($request->id) {
            $genre = Genre::find($request->id);
        } else {
            $genre = new Genre();
        }
        
        $genre->title = $request->title;
        $genre->save();

        return response()->json([
            'status' => true,
            'message' => 'Genre saved successfully'
        ]);
    }

    public function deleteGenre(Request $request)
    {
        $genre = Genre::find($request->id);
        if ($genre) {
            $genre->delete();
            return response()->json(['status' => true, 'message' => 'Genre deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Genre not found']);
    }

    public function CheckExistGenre(Request $request)
    {
        $exists = Genre::where('title', $request->title)
                      ->when($request->id, function($q) use ($request) {
                          $q->where('id', '!=', $request->id);
                      })
                      ->exists();
        
        return response()->json(['exists' => $exists]);
    }
} 