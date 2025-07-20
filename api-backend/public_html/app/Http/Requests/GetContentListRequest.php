<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class GetContentListRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     */
    public function rules(): array
    {
        return [
            'start' => 'required|integer|min:0',
            'limit' => 'required|integer|min:1|max:100',
            'type' => 'sometimes|integer|in:1,2', // 1 for movie, 2 for series based on Constants
            'genre_id' => 'sometimes|integer|exists:genres,id',
            'language_id' => 'sometimes|integer|exists:languages,id',
            'keyword' => 'sometimes|string|max:255',
        ];
    }

    /**
     * Get custom messages for validator errors.
     */
    public function messages(): array
    {
        return [
            'start.required' => 'Start position is required',
            'start.integer' => 'Start position must be a valid integer',
            'start.min' => 'Start position must be 0 or greater',
            'limit.required' => 'Limit is required',
            'limit.integer' => 'Limit must be a valid integer',
            'limit.min' => 'Limit must be at least 1',
            'limit.max' => 'Limit cannot exceed 100',
            'type.integer' => 'Type must be a valid integer',
            'type.in' => 'Type must be 1 (movie) or 2 (series)',
            'genre_id.integer' => 'Genre ID must be a valid integer',
            'genre_id.exists' => 'Genre does not exist',
            'language_id.integer' => 'Language ID must be a valid integer',
            'language_id.exists' => 'Language does not exist',
            'keyword.string' => 'Keyword must be a valid string',
            'keyword.max' => 'Keyword cannot exceed 255 characters',
        ];
    }

    /**
     * Get the validated data in a structured format
     */
    public function getFilters(): array
    {
        $filters = [
            'start' => $this->validated()['start'],
            'limit' => $this->validated()['limit'],
        ];

        if ($this->has('type')) {
            $filters['type'] = $this->validated()['type'];
        }

        if ($this->has('genre_id')) {
            $filters['genre_id'] = $this->validated()['genre_id'];
        }

        if ($this->has('language_id')) {
            $filters['language_id'] = $this->validated()['language_id'];
        }

        if ($this->has('keyword')) {
            $filters['keyword'] = $this->validated()['keyword'];
        }

        return $filters;
    }
} 