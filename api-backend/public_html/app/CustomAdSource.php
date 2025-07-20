<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CustomAdSource extends Model
{
    use HasFactory;
    protected $table = 'custom_ad_sources';
}
