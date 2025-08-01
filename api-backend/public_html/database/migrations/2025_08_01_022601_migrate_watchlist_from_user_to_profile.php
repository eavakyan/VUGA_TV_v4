<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        // First, backup existing watchlist data
        $watchlistData = DB::table('app_user_watchlist')->get();
        
        Schema::table('app_user_watchlist', function (Blueprint $table) {
            // Drop the existing foreign key constraint
            $table->dropForeign(['app_user_id']);
            
            // Drop the app_user_id column
            $table->dropColumn('app_user_id');
            
            // Add profile_id column
            $table->unsignedBigInteger('profile_id')->after('id');
            
            // Add foreign key constraint to app_user_profile
            $table->foreign('profile_id')->references('profile_id')->on('app_user_profile')->onDelete('cascade');
            
            // Add unique constraint to prevent duplicate entries
            $table->unique(['profile_id', 'content_id']);
        });
        
        // Migrate existing data to use the user's last active profile
        foreach ($watchlistData as $item) {
            $user = DB::table('app_user')->where('app_user_id', $item->app_user_id)->first();
            if ($user && $user->last_active_profile_id) {
                DB::table('app_user_watchlist')->insert([
                    'profile_id' => $user->last_active_profile_id,
                    'content_id' => $item->content_id,
                    'created_at' => $item->created_at,
                    'updated_at' => $item->updated_at
                ]);
            }
        }
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        // Backup profile-based data
        $watchlistData = DB::table('app_user_watchlist')->get();
        
        Schema::table('app_user_watchlist', function (Blueprint $table) {
            // Drop the foreign key constraint
            $table->dropForeign(['profile_id']);
            
            // Drop the unique constraint
            $table->dropUnique(['profile_id', 'content_id']);
            
            // Drop the profile_id column
            $table->dropColumn('profile_id');
            
            // Add back app_user_id column
            $table->unsignedBigInteger('app_user_id')->after('id');
            
            // Add back foreign key constraint
            $table->foreign('app_user_id')->references('app_user_id')->on('app_user')->onDelete('cascade');
        });
        
        // Migrate data back to user-based
        foreach ($watchlistData as $item) {
            $profile = DB::table('app_user_profile')->where('profile_id', $item->profile_id)->first();
            if ($profile) {
                // Check if this combination doesn't already exist
                $exists = DB::table('app_user_watchlist')
                    ->where('app_user_id', $profile->app_user_id)
                    ->where('content_id', $item->content_id)
                    ->exists();
                    
                if (!$exists) {
                    DB::table('app_user_watchlist')->insert([
                        'app_user_id' => $profile->app_user_id,
                        'content_id' => $item->content_id,
                        'created_at' => $item->created_at,
                        'updated_at' => $item->updated_at
                    ]);
                }
            }
        }
    }
};
