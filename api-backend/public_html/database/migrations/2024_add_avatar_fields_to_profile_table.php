<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class AddAvatarFieldsToProfileTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('app_user_profile', function (Blueprint $table) {
            // Add avatar_url if it doesn't exist
            if (!Schema::hasColumn('app_user_profile', 'avatar_url')) {
                $table->string('avatar_url')->nullable()->after('avatar_id');
            }
            
            // Add avatar_color if it doesn't exist
            if (!Schema::hasColumn('app_user_profile', 'avatar_color')) {
                $table->string('avatar_color', 7)->nullable()->after('avatar_url');
            }
            
            // Add custom_avatar_uploaded_at if it doesn't exist
            if (!Schema::hasColumn('app_user_profile', 'custom_avatar_uploaded_at')) {
                $table->timestamp('custom_avatar_uploaded_at')->nullable()->after('custom_avatar_url');
            }
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::table('app_user_profile', function (Blueprint $table) {
            $table->dropColumn(['avatar_url', 'avatar_color', 'custom_avatar_uploaded_at']);
        });
    }
}