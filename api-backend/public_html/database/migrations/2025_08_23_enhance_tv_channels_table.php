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
        Schema::table('tv_channel', function (Blueprint $table) {
            // Add new columns for enhanced Live TV functionality
            $table->string('logo_url')->nullable()->after('thumbnail');
            $table->string('stream_url', 500)->nullable()->after('source');
            $table->integer('channel_number')->nullable()->after('title');
            $table->boolean('is_active')->default(true)->after('access_type');
            $table->string('epg_url', 500)->nullable()->comment('Electronic Program Guide URL');
            $table->integer('total_views')->default(0);
            $table->integer('total_shares')->default(0);
            $table->string('language', 10)->default('en');
            $table->string('country_code', 5)->nullable();
            $table->text('description')->nullable();
            $table->json('streaming_qualities')->nullable()->comment('Available streaming qualities/bitrates');
            
            // Add indexes for better performance
            $table->index(['is_active', 'channel_number'], 'idx_active_channel_number');
            $table->index(['language'], 'idx_language');
            $table->index(['country_code'], 'idx_country');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('tv_channel', function (Blueprint $table) {
            $table->dropColumn([
                'logo_url',
                'stream_url',
                'channel_number',
                'is_active',
                'epg_url',
                'total_views',
                'total_shares',
                'language',
                'country_code',
                'description',
                'streaming_qualities'
            ]);
            
            $table->dropIndex('idx_active_channel_number');
            $table->dropIndex('idx_language');
            $table->dropIndex('idx_country');
        });
    }
};