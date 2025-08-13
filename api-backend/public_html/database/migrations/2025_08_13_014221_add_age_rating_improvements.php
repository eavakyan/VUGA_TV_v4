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
        // Add display_name and icon fields to age_limit table
        Schema::table('age_limit', function (Blueprint $table) {
            $table->string('display_name', 50)->nullable()->after('name');
            $table->string('icon', 50)->nullable()->after('display_name');
            $table->string('display_color', 7)->nullable()->after('icon');
        });

        // Create content_rating_reasons table
        Schema::create('content_rating_reasons', function (Blueprint $table) {
            $table->id('content_rating_reason_id');
            $table->unsignedBigInteger('content_id');
            $table->string('reason_type', 50); // violence, language, nudity, substance, frightening
            $table->enum('severity', ['mild', 'moderate', 'severe'])->default('moderate');
            $table->text('description')->nullable();
            $table->timestamps();

            $table->foreign('content_id')->references('content_id')->on('content')->onDelete('cascade');
            $table->index(['content_id', 'reason_type']);
        });

        // Update existing age_limit records with user-friendly display names
        $updates = [
            'AG_0_6' => ['display_name' => 'All Ages', 'icon' => 'family', 'display_color' => '#4CAF50'],
            'AG_7_12' => ['display_name' => '7+', 'icon' => 'child', 'display_color' => '#8BC34A'],
            'AG_13_16' => ['display_name' => '13+', 'icon' => 'teen', 'display_color' => '#FF9800'],
            'AG_17_18' => ['display_name' => '17+', 'icon' => 'mature', 'display_color' => '#F44336'],
            'AG_18_PLUS' => ['display_name' => '18+', 'icon' => 'adult', 'display_color' => '#9C27B0'],
        ];

        foreach ($updates as $code => $data) {
            DB::table('age_limit')->where('code', $code)->update($data);
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('content_rating_reasons');
        
        Schema::table('age_limit', function (Blueprint $table) {
            $table->dropColumn(['display_name', 'icon', 'display_color']);
        });
    }
};