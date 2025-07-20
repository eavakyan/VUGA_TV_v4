<?php

namespace App;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class TVCategory extends Authenticatable
{
	protected $table = 'tbl_tv_categories';
	public $primaryKey = 'category_id';
}
?>