<?php

namespace App;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Passport\HasApiTokens;

class Ads extends Authenticatable
{
	use HasApiTokens;
	protected $table = 'tbl_ads';
	public $primaryKey = 'ads_id';
	public $timestamps = true;
	public $incrementing = false;
}
