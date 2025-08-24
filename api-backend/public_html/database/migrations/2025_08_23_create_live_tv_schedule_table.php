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
        Schema::create('live_tv_schedule', function (Blueprint $table) {
            $table->id('schedule_id');
            $table->unsignedInteger('tv_channel_id');
            $table->string('program_title', 200);
            $table->text('description')->nullable();
            $table->string('thumbnail_url')->nullable();
            $table->string('genre', 100)->nullable();
            $table->timestamp('start_time');
            $table->timestamp('end_time');
            $table->boolean('is_repeat')->default(false);
            $table->string('episode_number', 20)->nullable();
            $table->string('season_number', 20)->nullable();
            $table->year('original_air_year')->nullable();
            $table->string('rating', 10)->nullable();
            $table->json('metadata')->nullable(); // Additional program metadata
            $table->timestamps();

            // Indexes for performance
            $table->index(['tv_channel_id', 'start_time', 'end_time'], 'idx_channel_time_range');
            $table->index(['start_time', 'end_time'], 'idx_time_range');
            $table->index(['program_title'], 'idx_program_title');
            $table->index(['genre'], 'idx_genre');

            // Foreign key constraint
            $table->foreign('tv_channel_id')->references('tv_channel_id')->on('tv_channel')->onDelete('cascade');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('live_tv_schedule');
    }
};