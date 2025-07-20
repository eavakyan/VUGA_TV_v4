<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class TermsCondition extends Model
{
    protected $table = 'terms_and_conditions';
    protected $primaryKey = 'id';

    protected $fillable = [
        'terms_condition',
        'created_at',
        'updated_at'
    ];
}
