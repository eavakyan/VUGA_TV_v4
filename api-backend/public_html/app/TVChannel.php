<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class TVChannel extends Model
{
	use HasFactory;
	protected $table = 'tv_channel';
	protected $primaryKey = 'tv_channel_id';
}
?>