

function ProfileDetail({ label, value, isEmail = false }) {
  return (
    <div className="flex justify-between text-md mb-2">
      <span className="font-medium text-gray-600">{label}:</span>
      { isEmail ? (
        <a href={`mailto:${value}`} className="text-blue-600 hover:underline">{value}</a>
      ) : (
        <span className="text-gray-900">{value}</span>
      )}
    </div>
  );
}

export default ProfileDetail;