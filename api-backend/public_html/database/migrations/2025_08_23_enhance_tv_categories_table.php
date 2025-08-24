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
        Schema::table('tv_category', function (Blueprint $table) {
            // Add new columns for better category management
            $table->string('slug', 100)->unique()->after('title');
            $table->integer('sort_order')->default(0)->after('image');
            $table->boolean('is_active')->default(true);
            $table->text('description')->nullable();
            $table->string('icon_url')->nullable()->after('image');
            $table->json('metadata')->nullable()->comment('Additional category metadata');
            
            // Add index for sorting and filtering
            $table->index(['is_active', 'sort_order'], 'idx_active_sort');
            $table->index(['slug'], 'idx_slug');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('tv_category', function (Blueprint $table) {
            $table->dropColumn([
                'slug',
                'sort_order',
                'is_active',
                'description',
                'icon_url',
                'metadata'
            ]);
            
            $table->dropIndex('idx_active_sort');
            $table->dropIndex('idx_slug');
        });
    }
};