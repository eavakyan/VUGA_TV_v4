<?php

namespace App\Http\Controllers;

use App\GlobalFunction;
use App\Notification;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    public function notification()
    {
        return view('notification');
    }

    public function notificationList(Request $request)
    {
        $columns = ['id'];
        $query = Notification::query();
        $totalData = $query->count();

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');


        if (!empty($searchValue)) {
            $query->where('title', 'LIKE', "%{$searchValue}%")
            ->orWhere('description', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
                        ->offset($start)
                        ->limit($limit)
                        ->get();

        $data = $result->map(function ($item) {
            
            $description = "<span class='itemDescription'>{$item->description}</span>";

            $repeat = "<a rel={$item->id}
                        data-title='{$item->title}' 
                        data-description={$item->description}
                        class='me-3 btn btn-info px-4 text-white repeat shadow-none'>" . __('repeat') . "</a>";
            $edit = "<a rel='{$item->id}'
                        data-title='{$item->title}' 
                        data-description='{$item->description}' 
                        class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";

            $actionHtml = "<div class='text-end action'>{$repeat} {$edit} {$delete}</div>";

            return [
                $item->title,
                $description,
                $actionHtml,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data
        ];

        return response()->json($json_data);
    }

    public function addNotification(Request $request)
    {
        $notification = new Notification();
        $notification->title = $request->title;
        $notification->description = $request->description;
        $notification->save();

        GlobalFunction::sendPushNotificationToAllUsers($request->title, $request->description, null);

        return response()->json([
            'status' => true,
            'message' => 'Notification Send Successfully',
        ]);
    }

    public function updateNotification(Request $request)
    {
        $notification = Notification::where('id', $request->notification_id)->first();

        if (!$notification) {
            return response()->json([
                'status' => false,
                'message' => 'Something went wrong',
            ]);
        }

        $notification->title = $request->title;
        $notification->description = $request->description;
        $notification->save();

        return response()->json([
            'status' => true,
            'message' => 'Notification Updated Successfully',
        ]);
    }

    public function repeatNotification(Request $request)
    {
        $title = $request->title;
        $description  = $request->description;

        GlobalFunction::sendPushNotificationToAllUsers($title, $description, null);

        return response()->json([
            'status' => true,
            'message' => 'Notification Send Successfully',
        ]);
    }

    public function deleteNotification(Request $request)
    {
        $notification = Notification::where('id', $request->notification_id)->first();
        if ($notification == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $notification->delete();

        return response()->json([
            'status' => true,
            'message' => 'Notification Deleted Successfully',
            'data' => $notification,
        ]);
    }
   
}
