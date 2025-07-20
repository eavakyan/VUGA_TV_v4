<?php

namespace App;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class Watchlist extends Authenticatable
{
	protected $table = 'tbl_watchlist';
	public $primaryKey = 'watchlist_id';

}
?>