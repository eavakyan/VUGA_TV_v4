<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class AddConsentFieldsToUsersTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::table('users', function (Blueprint $table) {
            // Add consent fields with default true for existing users
            $table->boolean('email_consent')->default(true)->after('is_premium');
            $table->boolean('sms_consent')->default(true)->after('email_consent');
            $table->timestamp('consent_updated_at')->nullable()->after('sms_consent');
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['email_consent', 'sms_consent', 'consent_updated_at']);
        });
    }
}