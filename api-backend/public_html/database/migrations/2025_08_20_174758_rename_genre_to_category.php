<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class RenameGenreToCategory extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        // Rename the genre table to category
        Schema::rename('genre', 'category');
        
        // Rename the primary key column
        Schema::table('category', function (Blueprint $table) {
            $table->renameColumn('genre_id', 'category_id');
        });
        
        // Rename the pivot table
        Schema::rename('content_genre', 'content_category');
        
        // Rename the foreign key column in the pivot table
        Schema::table('content_category', function (Blueprint $table) {
            $table->renameColumn('genre_id', 'category_id');
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        // Rename back to genre
        Schema::rename('category', 'genre');
        
        // Rename the primary key column back
        Schema::table('genre', function (Blueprint $table) {
            $table->renameColumn('category_id', 'genre_id');
        });
        
        // Rename the pivot table back
        Schema::rename('content_category', 'content_genre');
        
        // Rename the foreign key column back in the pivot table
        Schema::table('content_genre', function (Blueprint $table) {
            $table->renameColumn('category_id', 'genre_id');
        });
    }
}