<?php

namespace App;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class TVChannel extends Authenticatable
{
	protected $table = 'tbl_tv_channel';
	public $primaryKey = 'id';

	public function sources()
    {
        return $this->hasMany('App\TVChannelSource', 'channel_id', 'channel_id')->select( 'channel_id','source_type','source');
    }

	public static function get_random_string($field_code='channel_id')
	{
	  $random_unique  =  sprintf('%04X%04X', mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(16384, 20479), mt_rand(32768, 49151), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535));

	  $tvchannel = TVChannel::where('channel_id', '=', $random_unique)->first();
	  if ($tvchannel != null) {
		  $this->get_random_string();
	  }
	  return $random_unique;
  }
}
?>