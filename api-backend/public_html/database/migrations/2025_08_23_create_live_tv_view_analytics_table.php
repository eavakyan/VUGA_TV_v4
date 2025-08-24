<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('live_tv_view_analytics', function (Blueprint $table) {
            $table->id('analytics_id');
            $table->unsignedInteger('tv_channel_id');
            $table->unsignedInteger('app_user_id')->nullable();
            $table->integer('profile_id')->nullable();
            $table->enum('action_type', ['view', 'share', 'favorite'])->default('view');
            $table->string('device_type', 50)->nullable();
            $table->string('user_agent', 500)->nullable();
            $table->string('ip_address', 45)->nullable();
            $table->string('country', 5)->nullable();
            $table->integer('watch_duration')->default(0)->comment('Duration in seconds');
            $table->json('metadata')->nullable();
            $table->timestamp('created_at')->useCurrent();

            // Indexes for analytics queries
            $table->index(['tv_channel_id', 'created_at'], 'idx_channel_time');
            $table->index(['app_user_id', 'action_type'], 'idx_user_action');
            $table->index(['profile_id', 'action_type'], 'idx_profile_action');
            $table->index(['action_type', 'created_at'], 'idx_action_time');
            $table->index(['country', 'created_at'], 'idx_country_time');

            // Foreign key constraints
            $table->foreign('tv_channel_id')->references('tv_channel_id')->on('tv_channel')->onDelete('cascade');
            $table->foreign('app_user_id')->references('app_user_id')->on('app_user')->onDelete('set null');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('live_tv_view_analytics');
    }
};