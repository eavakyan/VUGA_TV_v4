<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\User;
use Illuminate\Http\Request;

class UserController extends Controller
{
    public function viewListUser()
    {
        return view('admin.user.list');
    }

    public function showUserList(Request $request)
    {
        $query = User::query();
        
        if ($request->has('search') && !empty($request->search)) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('fullname', 'LIKE', "%{$search}%")
                  ->orWhere('email', 'LIKE', "%{$search}%");
            });
        }

        $users = $query->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $users->items(),
            'recordsTotal' => $users->total(),
            'recordsFiltered' => $users->total()
        ]);
    }

    public function viewUser($id)
    {
        $user = User::findOrFail($id);
        return view('admin.user.view', compact('user'));
    }

    public function deleteUser(Request $request)
    {
        $user = User::find($request->id);
        if ($user) {
            $user->delete();
            return response()->json(['status' => true, 'message' => 'User deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'User not found']);
    }

    public function updateUserProfile(Request $request)
    {
        $user = User::find($request->id);
        if ($user) {
            $user->update($request->only(['fullname', 'email']));
            return response()->json(['status' => true, 'message' => 'User updated successfully']);
        }
        return response()->json(['status' => false, 'message' => 'User not found']);
    }
} 