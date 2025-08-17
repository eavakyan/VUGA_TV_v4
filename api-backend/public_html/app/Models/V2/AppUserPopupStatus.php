<?php

namespace App\Models\V2;

class AppUserPopupStatus extends BaseModel
{
    protected $table = 'app_user_popup_status';
    protected $primaryKey = 'app_user_popup_status_id';
    
    protected $fillable = [
        'app_user_id',
        'popup_definition_id',
        'popup_key',
        'status',
        'shown_at',
        'dismissed_at',
        'device_type'
    ];
    
    protected $casts = [
        'app_user_id' => 'integer',
        'popup_definition_id' => 'integer',
        'shown_at' => 'datetime',
        'dismissed_at' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the user this status belongs to
     */
    public function user()
    {
        return $this->belongsTo(AppUser::class, 'app_user_id');
    }
    
    /**
     * Get the popup definition
     */
    public function popupDefinition()
    {
        return $this->belongsTo(PopupDefinition::class, 'popup_definition_id');
    }
    
    /**
     * Scope for shown popups
     */
    public function scopeShown($query)
    {
        return $query->where('status', 'shown');
    }
    
    /**
     * Scope for dismissed popups
     */
    public function scopeDismissed($query)
    {
        return $query->where('status', 'dismissed');
    }
    
    /**
     * Mark as dismissed
     */
    public function markAsDismissed()
    {
        $this->update([
            'status' => 'dismissed',
            'dismissed_at' => now()
        ]);
    }
    
    /**
     * Mark as acknowledged
     */
    public function markAsAcknowledged()
    {
        $this->update([
            'status' => 'acknowledged',
            'dismissed_at' => now()
        ]);
    }
}