<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class TopContent extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'top_content';

    /**
     * The primary key associated with the table.
     *
     * @var string
     */
    protected $primaryKey = 'top_content_id';

    /**
     * The attributes that are mass assignable.
     *
     * @var array
     */
    protected $fillable = [
        'content_id',
        'content_index'
    ];

    /**
     * Timestamps are disabled
     *
     * @var bool
     */
    public $timestamps = false;

    /**
     * Get the content for this top content entry
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }
}