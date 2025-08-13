<?php

namespace App\Models\V2;

use Illuminate\Database\Eloquent\Model;

class ContentRatingReason extends Model
{
    protected $table = 'content_rating_reasons';
    protected $primaryKey = 'content_rating_reason_id';
    
    protected $fillable = [
        'content_id',
        'reason_type',
        'severity',
        'description'
    ];
    
    // Reason types
    const REASON_VIOLENCE = 'violence';
    const REASON_LANGUAGE = 'language';
    const REASON_NUDITY = 'nudity';
    const REASON_SUBSTANCE = 'substance';
    const REASON_FRIGHTENING = 'frightening';
    const REASON_SEXUAL = 'sexual';
    
    // Severity levels
    const SEVERITY_MILD = 'mild';
    const SEVERITY_MODERATE = 'moderate';
    const SEVERITY_SEVERE = 'severe';
    
    /**
     * Get the content this rating reason belongs to
     */
    public function content()
    {
        return $this->belongsTo(Content::class, 'content_id', 'content_id');
    }
    
    /**
     * Get user-friendly reason type display
     */
    public function getReasonDisplayAttribute()
    {
        $displays = [
            self::REASON_VIOLENCE => 'Violence',
            self::REASON_LANGUAGE => 'Language',
            self::REASON_NUDITY => 'Nudity',
            self::REASON_SUBSTANCE => 'Substance Use',
            self::REASON_FRIGHTENING => 'Frightening Scenes',
            self::REASON_SEXUAL => 'Sexual Content'
        ];
        
        return $displays[$this->reason_type] ?? $this->reason_type;
    }
    
    /**
     * Get severity display with icon
     */
    public function getSeverityDisplayAttribute()
    {
        $displays = [
            self::SEVERITY_MILD => 'Mild',
            self::SEVERITY_MODERATE => 'Moderate',
            self::SEVERITY_SEVERE => 'Severe'
        ];
        
        return $displays[$this->severity] ?? $this->severity;
    }
    
    /**
     * Get icon for reason type
     */
    public function getReasonIconAttribute()
    {
        $icons = [
            self::REASON_VIOLENCE => 'exclamationmark.triangle',
            self::REASON_LANGUAGE => 'speaker.wave.3',
            self::REASON_NUDITY => 'person.fill',
            self::REASON_SUBSTANCE => 'smoke',
            self::REASON_FRIGHTENING => 'eye.trianglebadge.exclamationmark',
            self::REASON_SEXUAL => 'heart.slash'
        ];
        
        return $icons[$this->reason_type] ?? 'questionmark.circle';
    }
}