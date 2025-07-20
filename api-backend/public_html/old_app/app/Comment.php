<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Comment extends Authenticatable
{
	use HasApiTokens;
	protected $table = 'tbl_comment';
	public $primaryKey = 'comment_id';
}
