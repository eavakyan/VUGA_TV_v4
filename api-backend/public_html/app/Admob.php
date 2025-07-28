<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Admob extends Model
{
    use HasFactory;
    protected $table = 'admob_config';
    protected $primaryKey = 'admob_config_id';
}
