<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class TVCategory extends Model
{
	use HasFactory;
	protected $table = 'tv_category';
	protected $primaryKey = 'tv_category_id';
}
?>