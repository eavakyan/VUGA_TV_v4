<?php

namespace App\Models\V2;

class PopupAnalytics extends BaseModel
{
    protected $table = 'popup_analytics';
    protected $primaryKey = 'popup_analytics_id';
    
    protected $fillable = [
        'popup_definition_id',
        'popup_key',
        'total_shown',
        'total_dismissed',
        'total_acknowledged',
        'last_calculated_at'
    ];
    
    protected $casts = [
        'popup_definition_id' => 'integer',
        'total_shown' => 'integer',
        'total_dismissed' => 'integer',
        'total_acknowledged' => 'integer',
        'last_calculated_at' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime'
    ];
    
    /**
     * Get the popup definition
     */
    public function popupDefinition()
    {
        return $this->belongsTo(PopupDefinition::class, 'popup_definition_id');
    }
    
    /**
     * Calculate and update analytics for a popup
     */
    public static function updateAnalyticsForPopup($popupDefinitionId)
    {
        $stats = AppUserPopupStatus::where('popup_definition_id', $popupDefinitionId)
            ->selectRaw('
                COUNT(*) as total_shown,
                COUNT(CASE WHEN status = "dismissed" THEN 1 END) as total_dismissed,
                COUNT(CASE WHEN status = "acknowledged" THEN 1 END) as total_acknowledged
            ')
            ->first();
        
        $popup = PopupDefinition::find($popupDefinitionId);
        
        static::updateOrCreate(
            ['popup_definition_id' => $popupDefinitionId],
            [
                'popup_key' => $popup->popup_key,
                'total_shown' => $stats->total_shown,
                'total_dismissed' => $stats->total_dismissed,
                'total_acknowledged' => $stats->total_acknowledged,
                'last_calculated_at' => now()
            ]
        );
    }
    
    /**
     * Get dismissal rate percentage
     */
    public function getDismissalRateAttribute()
    {
        if ($this->total_shown === 0) {
            return 0;
        }
        
        return round(($this->total_dismissed / $this->total_shown) * 100, 2);
    }
    
    /**
     * Get acknowledgment rate percentage
     */
    public function getAcknowledgmentRateAttribute()
    {
        if ($this->total_shown === 0) {
            return 0;
        }
        
        return round(($this->total_acknowledged / $this->total_shown) * 100, 2);
    }
}