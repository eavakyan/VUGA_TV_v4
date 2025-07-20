<?php

namespace App;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class TVChannelSource extends Authenticatable
{
	protected $table = 'tbl_tv_channel_source';
	public $primaryKey = 'source_id';

}
?>