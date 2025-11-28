import React from 'react';

interface FormFieldProps {
  id: string;
  name: string;
  type: string;
  label?: string;
  placeholder?: string;
  value: string;
  error?: string;
  autoComplete?: string;
  required?: boolean;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  className?: string;
}

/**
 * 共通フォームフィールドコンポーネント
 */
export const FormField: React.FC<FormFieldProps> = ({
  id,
  name,
  type,
  label,
  placeholder,
  value,
  error,
  autoComplete,
  required = true,
  onChange,
  className = '',
}) => {
  const baseClassName = `mt-1 appearance-none block w-full px-3 py-2 border ${
    error ? 'border-red-300' : 'border-gray-300'
  } rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm`;

  return (
    <div className={className}>
      {label && (
        <label htmlFor={id} className="block text-sm font-medium text-gray-700">
          {label}
        </label>
      )}
      <input
        id={id}
        name={name}
        type={type}
        autoComplete={autoComplete}
        required={required}
        className={baseClassName}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
      />
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  );
};
