<?php

namespace App\Http\Controllers;

use App\Genre;
use Illuminate\Http\Request;

class GenreController extends Controller
{
    public function genres()
    {
        return view('genres');
    }

    public function genresList(Request $request)
    {
        $query = Genre::query();
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
            $edit = "<a rel='{$item->id}' data-title='{$item->title}' class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";
            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";
            $action = "<div class='text-end action'>{$edit}{$delete}</div>";
            return [
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

    public function addGenre(Request $request)
    {
         
        if ($request->has('genres')) {
           
            $genresString = $request->genres;
            $genres = explode(', ', $genresString);

            $genre_ids = [];

            foreach ($genres as $genreName) {
                $genreName = trim($genreName);
                $existingGenre = Genre::where('title', $genreName)->first();

                if (!$existingGenre) {
                    $existingGenre = new Genre();
                    $existingGenre->title = $genreName;
                    $existingGenre->save();
                } 

                array_push($genre_ids, $existingGenre);
            }

            $allGenres = Genre::orderBy('created_at', 'DESC')->get();
            
            return response()->json([
                'status' => true,
                'message' => 'Genres processed successfully',
                'data' => $genre_ids,
                'allGenres' => $allGenres,
            ]);
        } else {
            
            $genre = new Genre();
            $genre->title = $request->title;
            $genre->save();

            return response()->json([
                'status' => true,
                'message' => 'Genre Added Successfully',
                'data' => $genre,
            ]);
        }
    }


    public function updateGenre(Request $request)
    {
        $genre = Genre::where('id', $request->genre_id)->first();
        if ($genre == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $genre->title = $request->title;
        $genre->save();

        return response()->json([
            'status' => true,
            'message' => 'Genre Updated Successfully',
            'data' => $genre,
        ]);
    }

    public function deleteGenre(Request $request)
    {
        $genre = Genre::where('id', $request->genre_id)->first();
        if ($genre == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        $genre->delete();
        
        return response()->json([
            'status' => true,
            'message' => 'Genre Deleted Successfully',
            'data' => $genre,
        ]);
    }
 
}
