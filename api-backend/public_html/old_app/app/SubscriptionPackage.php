<?php

namespace App;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class SubscriptionPackage extends Authenticatable
{
	protected $table = 'tbl_subscription_package';
	public $primaryKey = 'package_id';

}
?>