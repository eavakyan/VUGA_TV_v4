<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // Create popup definitions table
        Schema::create('popup_definition', function (Blueprint $table) {
            $table->id('popup_definition_id');
            $table->string('popup_key', 100)->unique()->comment('Unique identifier for popup type');
            $table->string('title', 255);
            $table->text('content');
            $table->enum('popup_type', ['info', 'feature', 'warning', 'promotion', 'onboarding'])->default('info');
            $table->json('target_audience')->nullable()->comment('Targeting rules JSON');
            $table->tinyInteger('is_active')->default(1);
            $table->integer('priority')->default(0)->comment('Higher numbers shown first');
            $table->timestamps();
            
            $table->index(['is_active', 'priority']);
            $table->index('popup_key');
            $table->index('target_audience', 'idx_target_audience');
        });
        
        // Create user popup status tracking table
        Schema::create('app_user_popup_status', function (Blueprint $table) {
            $table->id('app_user_popup_status_id');
            $table->unsignedBigInteger('app_user_id');
            $table->unsignedBigInteger('popup_definition_id');
            $table->string('popup_key', 100)->comment('Denormalized for fast lookups');
            $table->enum('status', ['shown', 'dismissed', 'acknowledged'])->default('shown');
            $table->timestamp('shown_at')->default(DB::raw('CURRENT_TIMESTAMP'));
            $table->timestamp('dismissed_at')->nullable();
            $table->string('device_type', 50)->nullable()->comment('iOS, Android, AndroidTV');
            $table->timestamps();
            
            $table->foreign('app_user_id')->references('app_user_id')->on('app_user')->onDelete('cascade');
            $table->foreign('popup_definition_id')->references('popup_definition_id')->on('popup_definition')->onDelete('cascade');
            
            $table->unique(['app_user_id', 'popup_definition_id'], 'unique_user_popup');
            $table->index(['app_user_id', 'status']);
            $table->index(['popup_key', 'app_user_id']);
            $table->index('shown_at');
            $table->index('device_type');
        });
        
        // Create analytics table
        Schema::create('popup_analytics', function (Blueprint $table) {
            $table->id('popup_analytics_id');
            $table->unsignedBigInteger('popup_definition_id');
            $table->string('popup_key', 100);
            $table->integer('total_shown')->default(0);
            $table->integer('total_dismissed')->default(0);
            $table->integer('total_acknowledged')->default(0);
            $table->timestamp('last_calculated_at')->default(DB::raw('CURRENT_TIMESTAMP'));
            $table->timestamps();
            
            $table->foreign('popup_definition_id')->references('popup_definition_id')->on('popup_definition')->onDelete('cascade');
            $table->unique('popup_definition_id');
            $table->index('popup_key');
        });
        
        // Insert sample popup definitions
        $this->insertSamplePopups();
    }
    
    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('popup_analytics');
        Schema::dropIfExists('app_user_popup_status');
        Schema::dropIfExists('popup_definition');
    }
    
    /**
     * Insert sample popup definitions
     */
    private function insertSamplePopups()
    {
        $samplePopups = [
            [
                'popup_key' => 'welcome_new_user',
                'title' => 'Welcome to VUGA TV!',
                'content' => 'Discover thousands of movies and TV shows. Create profiles for your family and enjoy personalized recommendations.',
                'popup_type' => 'onboarding',
                'target_audience' => json_encode(['user_type' => 'new']),
                'priority' => 100,
                'is_active' => 1
            ],
            [
                'popup_key' => 'create_profile_prompt',
                'title' => 'Create Your Profile',
                'content' => 'Personalize your experience! Create a profile to get better recommendations and track your watching progress.',
                'popup_type' => 'feature',
                'target_audience' => json_encode(['user_type' => 'new']),
                'priority' => 90,
                'is_active' => 1
            ],
            [
                'popup_key' => 'premium_upgrade_prompt',
                'title' => 'Upgrade to Premium',
                'content' => 'Get unlimited access to all content, HD streaming, and exclusive shows with VUGA Premium.',
                'popup_type' => 'promotion',
                'target_audience' => json_encode(['subscription_status' => 'free']),
                'priority' => 80,
                'is_active' => 1
            ],
            [
                'popup_key' => 'parental_controls_info',
                'title' => 'Set Up Parental Controls',
                'content' => 'Keep your family safe with age-appropriate content filters and kids profiles.',
                'popup_type' => 'info',
                'target_audience' => json_encode(['user_type' => 'existing']),
                'priority' => 70,
                'is_active' => 1
            ],
            [
                'popup_key' => 'download_feature_announcement',
                'title' => 'New: Offline Downloads',
                'content' => 'Download your favorite shows and movies to watch offline, anytime, anywhere!',
                'popup_type' => 'feature',
                'target_audience' => json_encode(['device_types' => ['iOS', 'Android']]),
                'priority' => 60,
                'is_active' => 1
            ]
        ];
        
        foreach ($samplePopups as $popup) {
            DB::table('popup_definition')->insert(array_merge($popup, [
                'created_at' => now(),
                'updated_at' => now()
            ]));
        }
        
        // Initialize analytics for sample popups
        $popupIds = DB::table('popup_definition')->pluck('popup_definition_id', 'popup_key');
        foreach ($popupIds as $key => $id) {
            DB::table('popup_analytics')->insert([
                'popup_definition_id' => $id,
                'popup_key' => $key,
                'total_shown' => 0,
                'total_dismissed' => 0,
                'total_acknowledged' => 0,
                'last_calculated_at' => now(),
                'created_at' => now(),
                'updated_at' => now()
            ]);
        }
    }
};