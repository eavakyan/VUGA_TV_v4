<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Admin extends Model
{
	use HasFactory;
	protected $table = 'admin_user';
	protected $primaryKey = 'admin_user_id';
}
