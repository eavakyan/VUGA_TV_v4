<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateTvAuthSessionsTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('tv_auth_sessions', function (Blueprint $table) {
            $table->id('tv_auth_session_id');
            $table->string('session_token', 64)->unique();
            $table->text('qr_code');
            $table->string('tv_device_id', 255);
            $table->enum('status', ['pending', 'authenticated', 'expired'])->default('pending');
            $table->unsignedBigInteger('app_user_id')->nullable();
            $table->timestamp('expires_at');
            $table->timestamp('authenticated_at')->nullable();
            $table->timestamps();
            
            $table->foreign('app_user_id')->references('app_user_id')->on('app_user')->onDelete('cascade');
            $table->index('session_token');
            $table->index('tv_device_id');
            $table->index('status');
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('tv_auth_sessions');
    }
}