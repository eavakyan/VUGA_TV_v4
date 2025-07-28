<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class GlobalSettings extends Model
{
    use HasFactory;
    protected $table = 'global_setting';
    protected $primaryKey = 'global_setting_id';
}
