<?php

namespace App\Http\Controllers\admin;

use Illuminate\Http\Request;
use App\Http\Controllers\Controller;
use Redirect;
use URL;
use Hash;
use Session;
use DB;
use App\Admin;
use App\User;
use App\Settings;
use App\Notification;
use App\SubscriptionPackage;
use App\Ads;
use App\Common;
use App\CustomAds;
use App\GlobalFunction;
use File;

class SettingsController extends Controller
{

	public function viewSubscriptionPackage()
	{
		$monthlyData = SubscriptionPackage::where('duration', 1)->first();
		$yearlyData = SubscriptionPackage::where('duration', 2)->first();
		return view('admin.settings.subscription_package')->with('monthlyData', $monthlyData)->with('yearlyData', $yearlyData);
	}

	public function addUpdateSubscriptionPackage(Request $request)
	{

		$price = $request->input('price');
		$currency = $request->input('currency');
		$days = $request->input('days');
		$android_product_id = $request->input('android_product_id');
		$ios_product_id = $request->input('ios_product_id');

		$duration = $request->input('duration');
		$id = $request->input('hidden_id');
		$data['duration'] = $duration;
		$data['price'] = $price;
		$data['currency'] = $currency;
		$data['days'] = $days;
		$data['android_product_id'] = $android_product_id;
		$data['ios_product_id'] = $ios_product_id;

		if ($duration == 1) {
			if (!empty($id)) {
				$result = SubscriptionPackage::where('package_id', $id)->where('duration', 1)->update($data);
				$msg = "Update";
			} else {
				$result = SubscriptionPackage::insert($data);
				$msg = "Add";
				$id = DB::getPdo()->lastInsertId();
			}
		} else {
			if (!empty($id)) {
				$result = SubscriptionPackage::where('package_id', $id)->where('duration', 2)->update($data);
				$msg = "Update";
			} else {
				$result = SubscriptionPackage::insert($data);
				$msg = "Add";
				$id = DB::getPdo()->lastInsertId();
			}
		}

		if ($result) {
			$response['success'] = 1;
			$response['duration'] = $duration;
			$response['id'] = $id;
			$response['message'] = "Successfully " . $msg . " SubscriptionPackage";
		} else {
			$response['success'] = 0;
			$response['duration'] = $duration;
			$response['id'] = 0;
			$response['message'] = "Error While " . $msg . " SubscriptionPackage";
		}
		// print_r($response);die;
		echo json_encode($response);
	}

	public function viewAds()
	{
		$AdsData =  Ads::first();
		return view('admin.settings.ads')->with('AdsData', $AdsData);
	}
	public function viewCustomAds()
	{
		$customAddsCount = CustomAds::count();
		return view('admin.settings.customAds')->with('customAdsCount', $customAddsCount);
	}

	public function addUpdateAndriodAds(Request $request)
	{

		$android_admob_banner_id = $request->input('android_admob_banner_id');
		$android_admob_interestitial_id = $request->input('android_admob_interestitial_id');
		$android_admob_native_id = $request->input('android_admob_native_id');
		$android_admob_rewarded_id = $request->input('android_admob_rewarded_id');

		$id = $request->input('hidden_id');
		$data['android_admob_banner_id'] = $android_admob_banner_id;
		$data['android_admob_interestitial_id'] = $android_admob_interestitial_id;
		$data['android_admob_native_id'] = $android_admob_native_id;
		$data['android_admob_rewarded_id'] = $android_admob_rewarded_id;

		if (!empty($id)) {
			$result = Ads::where('ads_id', $id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result = Ads::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
			$id = DB::getPdo()->lastInsertId();
		}

		if ($result) {
			$response['success'] = 1;
			$response['data'] = $id;
			$response['message'] = "Successfully " . $msg . " Ads";
		} else {
			$response['success'] = 0;
			$response['data'] = 0;
			$response['message'] = "Error While " . $msg . " Ads";
		}
		echo json_encode($response);
	}

	public function addUpdateIosAds(Request $request)
	{

		$ios_admob_banner_id = $request->input('ios_admob_banner_id');
		$ios_admob_interestitial_id = $request->input('ios_admob_interestitial_id');
		$ios_admob_native_id = $request->input('ios_admob_native_id');
		$ios_admob_rewarded_id = $request->input('ios_admob_rewarded_id');

		$id = $request->input('hidden_id');
		$data['ios_admob_banner_id'] = $ios_admob_banner_id;
		$data['ios_admob_interestitial_id'] = $ios_admob_interestitial_id;
		$data['ios_admob_native_id'] = $ios_admob_native_id;
		$data['ios_admob_rewarded_id'] = $ios_admob_rewarded_id;

		if (!empty($id)) {
			$result = Ads::where('ads_id', $id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result = Ads::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
			$id = DB::getPdo()->lastInsertId();
		}

		if ($result) {
			$response['success'] = 1;
			$response['data'] = $id;
			$response['message'] = "Successfully " . $msg . " Ads";
		} else {
			$response['success'] = 0;
			$response['data'] = 0;
			$response['message'] = "Error While " . $msg . " Ads";
		}
		echo json_encode($response);
	}

	public function viewSettings()
	{
		$SettingData =  Settings::first();
		return view('admin.settings.settings')->with('SettingData', $SettingData);
	}

	public function addUpdateSetting(Request $request)
	{

		$flag = $request->input('flag');
		$terms_url = $request->input('terms_url');
		$google_play_licence_key = $request->input('google_play_licence_key');
		$more_apps_url = $request->input('more_apps_url');
		$privacy_url = $request->input('privacy_url');
		$app_name = $request->input('app_name');
		$is_live_tv_enable = $request->input('is_live_tv_enable');

		$is_custom_ios = $request->input('is_custom_ios');
		$is_custom_and = $request->input('is_custom_and');
		$is_admob_ios = $request->input('is_admob_ios');
		$is_admob_and = $request->input('is_admob_and');
		$videoad_skip_time = $request->input('videoad_skip_time');

		$id = $request->input('hidden_id');
		if ($flag == 1) {
			$data['more_apps_url'] = $more_apps_url;
			$data['privacy_url'] = $privacy_url;
			$data['terms_url'] = $terms_url;
			$data['google_play_licence_key'] = $google_play_licence_key;
		} else {
			$data['app_name'] = $app_name;
			$data['is_custom_ios'] = $is_custom_ios == 'on' ? 1 : 0;
			$data['is_custom_and'] = $is_custom_and == 'on' ? 1 : 0;
			$data['is_admob_ios'] = $is_admob_ios == 'on' ? 1 : 0;
			$data['is_admob_and'] = $is_admob_and == 'on' ? 1 : 0;
			$data['is_live_tv_enable'] = $is_live_tv_enable == 'on' ? 1 : 0;
			$data['videoad_skip_time'] = $videoad_skip_time;
		}

		if (!empty($id)) {
			$result = Settings::where('id', $id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
		} else {
			$result = Settings::insert($data);
			$msg = "Add";
			$response['flag'] = 1;
			$id = DB::getPdo()->lastInsertId();
		}

		if ($result) {
			$response['success'] = 1;
			$response['data'] = $id;
			$response['message'] = "Successfully " . $msg . " Setting";
		} else {
			$response['success'] = 0;
			$response['data'] = 0;
			$response['message'] = "Error While " . $msg . " Setting";
		}
		echo json_encode($response);
	}

	public function sendNotification(Request $request)
	{
		$notification_topic = 'flixy';
		$notification_title = $request->input('notification_title');
		$notification_message = $request->input('notification_message');
		$notification_image = "";
		if ($request->has("notify_image")) {
			$notification_image = GlobalFunction::saveFileAndGivePath($request->file('notify_image'));
		}


		$is_send = Common::send_push($notification_topic, $notification_title, $notification_message, 0, $notification_image, 1);

		if ($is_send) {

			$response['success'] = 1;
			$response['message'] = "Successfully Send Notification";
		} else {
			$response['success'] = 0;
			$response['message'] = "Error While Send Notification";
		}
		echo json_encode($response);
	}

	public function viewListNotification()
	{
		$total_notification = Notification::count();
		return view('admin.settings.notification_list')->with('total_notification', $total_notification);
	}

	public function UpdateNotification(Request $request)
	{
		$notification_id = $request->input('notification_id');

		$data['title'] = $request->input('notification_title');
		$data['message'] = $request->input('notification_message');

		if (!empty($notification_id)) {
			$result =  Notification::where('notification_id', $notification_id)->update($data);
			$msg = "Update";
			$response['flag'] = 2;
			$result = Notification::where('notification_id', $notification_id)->first();
			$total_notification = Notification::count();
			if ($result) {
				$response['data'] = $result;
				$response['success'] = 1;
				$response['message'] = "Successfully " . $msg . " Notification";
				$response['total_notification'] = $total_notification;
			} else {
				$response['data'] = "";
				$response['success'] = 0;
				$response['message'] = "Error While " . $msg . " Notification";
				$response['total_notification'] = 0;
			}
		} else {
			$notification_topic = 'Learny';
			$notification_title = $request->input('notification_title');
			$notification_message = $request->input('notification_message');

			$is_send = Common::send_push($notification_topic, $notification_title, $notification_message, 0, $notification_image, 1);

			$msg = "Send";
			$response['flag'] = 1;

			if ($is_send) {

				$notificationdata = array(
					'title' => $notification_title,
					'message' => $notification_message,
					// 'icon' => $imagename
				);
				Notification::insert($notificationdata);

				$result = Notification::where('notification_id', $notification_id)->first();
				$total_notification = Notification::count();
				$response['data'] = $result;
				$response['success'] = 1;
				$response['message'] = "Successfully " . $msg . " Notification";
				$response['total_notification'] = $total_notification;
			} else {
				$response['data'] = "";
				$response['success'] = 0;
				$response['message'] = "Error While " . $msg . " Notification";
				$response['total_notification'] = 0;
			}
		}

		echo json_encode($response);
	}

	public function deleteNotification(Request $request)
	{

		$notification_id = $request->input('notification_id');
		$result =  Notification::where('notification_id', $notification_id)->delete();
		$total_notification = Notification::count();
		if ($result) {
			$response['success'] = 1;
			$response['total_notification'] = $total_notification;
		} else {
			$response['success'] = 0;
			$response['total_notification'] = 0;
		}
		echo json_encode($response);
	}

	public function showNotificationList(Request $request)
	{

		$columns = array(
			0 => 'title',
			1 => 'message',
			2 => 'notification_id',
		);

		$totalData = Notification::count();

		$totalFiltered = $totalData;

		$limit = $request->input('length');
		$start = $request->input('start');
		$order = $columns[$request->input('order.0.column')];
		$dir = $request->input('order.0.dir');

		if (empty($request->input('search.value'))) {
			$NotificationData = Notification::offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();
		} else {
			$search = $request->input('search.value');

			$NotificationData =  Notification::where('notification_id', 'LIKE', "%{$search}%")->orWhere('message', 'LIKE', "%{$search}%")->orWhere('title', 'LIKE', "%{$search}%")
				->offset($start)
				->limit($limit)
				->orderBy($order, $dir)
				->get();

			$totalData  = $totalFiltered = Notification::where('notification_id', 'LIKE', "%{$search}%")->orWhere('message', 'LIKE', "%{$search}%")->orWhere('title', 'LIKE', "%{$search}%")
				->count();
		}

		$data = array();
		if (!empty($NotificationData)) {
			foreach ($NotificationData as $rows) {
				if ($rows->icon) {
					$url = '<img class="img-lg rounded" src="' . url(env('DEFAULT_IMAGE_URL') . $rows->icon) . '" width="60" height="60"/>';
				} else {
					$url = '';
				}
				if (Session::get('admin_id') == 2) {
					$disabled = "disabled";
				} else {
					$disabled = "";
				}
				$data[] = array(
					$rows->title,
					$rows->message,
					$url,
					'<button class="UpdateNotification btn btn-info" data-toggle="modal" data-target="#notificationModal" data-id="' . $rows->notification_id . '" data-title="' . $rows->title . '" data-message="' . $rows->message . '" data-img="' . url(env('DEFAULT_IMAGE_URL') . $rows->icon) . '">Edit</button>
					<button class="delete btn btn-danger" id="DeleteNotification" data-id="' . $rows->notification_id . '">Delete</button>'
				);
			}
		}

		$json_data = array(
			"draw"            => intval($request->input('draw')),
			"recordsTotal"    => intval($totalData),
			"recordsFiltered" => intval($totalFiltered),
			"data"            => $data
		);

		echo json_encode($json_data);
		exit();
	}
}
