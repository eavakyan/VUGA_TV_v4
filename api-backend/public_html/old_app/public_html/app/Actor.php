<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Actor extends Authenticatable
{
	use HasApiTokens;
	protected $table = 'tbl_actor';
	public $primaryKey = 'actor_id';
	public $timestamps = true;
	public $incrementing = false;
}
