<?php

namespace App\Models\V2;

class Actor extends BaseModel
{
    protected $table = 'actor';
    protected $primaryKey = 'actor_id';
    
    protected $fillable = [
        'fullname',
        'dob',
        'bio',
        'profile_image'
    ];
    
    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the actor's content appearances
     */
    public function contents()
    {
        return $this->belongsToMany(Content::class, 'content_cast', 'actor_id', 'content_id')
                    ->withPivot('character_name');
    }
    
    /**
     * Get the actor's cast records
     */
    public function castRecords()
    {
        return $this->hasMany(ContentCast::class, 'actor_id');
    }
}