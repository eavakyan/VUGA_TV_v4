<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Actor extends Model
{
	use HasFactory;
	protected $table = 'actor';
	protected $primaryKey = 'actor_id';
	
	// Add accessor for backward compatibility with 'id' field
	public function getIdAttribute()
	{
		return $this->actor_id;
	}
}
