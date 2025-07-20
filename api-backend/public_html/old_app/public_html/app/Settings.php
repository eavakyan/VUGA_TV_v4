<?php

namespace App;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class Settings extends Authenticatable
{
	protected $table = 'tbl_settings';
	public $primaryKey = 'id';
}
?>