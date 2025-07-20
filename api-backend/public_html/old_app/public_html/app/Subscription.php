<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Subscription extends Authenticatable
{
	use HasApiTokens;
	protected $table = 'tbl_subscription';
	public $primaryKey = 'id';
	public $timestamps = true;
	public $incrementing = false;

	public static function get_random_string($field_code='subscription_id')
	{
        $random_unique  =  sprintf('%04X%04X', mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(16384, 20479), mt_rand(32768, 49151), mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535));

        $purchase = Subscription::where('subscription_id', '=', $random_unique)->first();
        if ($purchase != null) {
            Subscription::gget_random_string();
        }
        return $random_unique;
    }
}
