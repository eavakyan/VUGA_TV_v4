<?php

namespace App\Http\Controllers\admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use Session;
use DB;
use App\Content;
use App\User;
use App\Genre;

class GenreController extends Controller
{

	public function viewListGenre()
	{
		$total_genre = Genre::count();
		return view('admin.genre.genre_list')->with('total_genre',$total_genre);
    }

    public function CheckExistGenre(Request $request)
	{
		$genre_name = $request->input('genre_name');
		$genre_id = $request->input('genre_id');

		if(!empty($genre_id)){
			$checkGenre = Genre::selectRaw('*')->where('genre_name',$genre_name)->where('genre_id','!=',$genre_id)->first();
		}else{
			$checkGenre = Genre::selectRaw('*')->where('genre_name',$genre_name)->first();
		}

		if(!empty($checkGenre)) {
			return json_encode(FALSE);
		}else{
			return json_encode(TRUE);
		}
	}

    
	public function addUpdateGenre(Request $request){
		$genre_id = $request->input('genre_id');
		$genre_name = $request->input('genre_name');

		$data['genre_name'] = $genre_name;

		if(!empty($genre_id)){
			$result =  Genre::where('genre_id',$genre_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		}else{
			$result =  Genre::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
		}
		$total_genre = Genre::count();
		if ($result) {
			$response['success'] = 1;
			$response['message'] = "Successfully ".$msg." Genre";
			$response['total_genre'] = $total_genre;
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While ".$msg." Genre";
			$response['total_genre'] = 0;
		}
		echo json_encode($response);
	}

	public function deleteGenre(Request $request){

		$genre_id = $request->input('genre_id');
		$result =  Genre::where('genre_id',$genre_id)->delete();
		$total_genre = Genre::count();

		if ($result) {
			$response['success'] = 1;
			$response['total_genre'] = $total_genre;
		} else {
			$response['success'] = 0;
			$response['total_genre'] = 0;
		}
		echo json_encode($response);

	}

	public function showGenreList(Request $request)
    {

		$columns = array( 
            0=>'genre_name',
		);

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if(empty($request->input('search.value')))
		{      
			$GenreData = Genre::offset($start)
					->limit($limit)
					->orderBy($order,$dir)
					->get();
            $totalData = $totalFiltered = Genre::count();
		}
		else {
			$search = $request->input('search.value'); 
			$GenreData =  Genre::where('genre_id','LIKE',"%{$search}%")
							->orWhere('genre_name', 'LIKE',"%{$search}%")
							->offset($start)
							->limit($limit)
							->orderBy($order,$dir)
							->get();

            $totalData = $totalFiltered = Genre::where('genre_id','LIKE',"%{$search}%")
                        ->orWhere('genre_name', 'LIKE',"%{$search}%")                 
                        ->count();
		}

		$data = array();
		if(!empty($GenreData))
		{
			foreach ($GenreData as $rows)
			{

				if(Session::get('admin_id') == 2){ 
					$disabled = "disabled";
				}else{
					$disabled = "";
				}

				$data[]= array(
                    $rows->genre_name,
                    '<a class="UpdateGenre" data-toggle="modal" data-target="#genreModal" data-id="'.$rows->genre_id.'" data-genre_name="'.$rows->genre_name.'" ><i class="i-cl-3 fa fa-edit col-blue font-20 pointer p-l-5 p-r-5"></i></a>
					<a class="delete DeleteGenre" data-id="'.$rows->genre_id.'" ><i class="fa fa-trash text-danger font-20 pointer p-l-5 p-r-5"></i></a>'
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
