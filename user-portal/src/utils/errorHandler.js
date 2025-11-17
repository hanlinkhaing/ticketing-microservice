// Error handling utility for frontend
export const handleApiError = (error, serviceName) => {
  console.error(`[ERROR] [${serviceName}] API Error:`, error);
  
  let errorMessage = 'An unexpected error occurred';
  let errorDetails = {
    service: serviceName,
    timestamp: new Date().toISOString()
  };
  
  if (error.response) {
    // The request was made and the server responded with a status code
    // that falls out of the range of 2xx
    console.error(`[ERROR] [${serviceName}] Response data:`, error.response.data);
    console.error(`[ERROR] [${serviceName}] Response status:`, error.response.status);
    console.error(`[ERROR] [${serviceName}] Response headers:`, error.response.headers);
    
    errorMessage = error.response.data?.message || error.response.data?.error || `HTTP ${error.response.status} Error`;
    errorDetails = {
      ...errorDetails,
      status: error.response.status,
      statusText: error.response.statusText,
      data: error.response.data,
      requestId: error.response.data?.requestId,
      service: error.response.data?.service || serviceName
    };
  } else if (error.request) {
    // The request was made but no response was received
    console.error(`[ERROR] [${serviceName}] No response received:`, error.request);
    errorMessage = 'No response from server - service may be down';
    errorDetails = {
      ...errorDetails,
      type: 'NO_RESPONSE',
      message: 'The request was made but no response was received'
    };
  } else {
    // Something happened in setting up the request that triggered an Error
    console.error(`[ERROR] [${serviceName}] Request setup error:`, error.message);
    errorMessage = error.message || 'Error setting up the request';
    errorDetails = {
      ...errorDetails,
      type: 'REQUEST_SETUP_ERROR',
      message: error.message
    };
  }
  
  console.error(`[ERROR] [${serviceName}] Processed error details:`, errorDetails);
  
  return {
    message: errorMessage,
    details: errorDetails,
    toString: () => `${errorMessage} (Service: ${serviceName}, Request ID: ${errorDetails.requestId || 'N/A'})`
  };
};

// Centralized API service with error handling
export const apiService = async (url, options = {}, serviceName = 'unknown') => {
  const requestId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
  
  console.log(`[API] [${serviceName}] [${requestId}] Starting request:`, {
    url,
    method: options.method || 'GET',
    headers: options.headers,
    body: options.body
  });
  
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'X-Request-ID': requestId,
        ...options.headers
      }
    });
    
    console.log(`[API] [${serviceName}] [${requestId}] Response received:`, {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries())
    });
    
    if (!response.ok) {
      const error = new Error(`HTTP ${response.status} ${response.statusText}`);
      error.response = response;
      error.requestId = requestId;
      throw error;
    }
    
    const data = await response.json();
    
    console.log(`[API] [${serviceName}] [${requestId}] Request completed successfully:`, {
      data: data
    });
    
    return data;
    
  } catch (error) {
    console.error(`[API] [${serviceName}] [${requestId}] Request failed:`, error);
    
    // Enhance error with service name
    const enhancedError = handleApiError(error, serviceName);
    enhancedError.details.requestId = requestId;
    
    throw enhancedError;
  }
};

// Toast notification utility
export const showErrorNotification = (error, serviceName) => {
  const errorInfo = typeof error === 'string' ? { message: error } : error;
  const message = errorInfo.message || 'An error occurred';
  const service = errorInfo.details?.service || serviceName;
  const requestId = errorInfo.details?.requestId || 'N/A';
  
  console.error(`[NOTIFICATION] [${service}] Error: ${message} (Request ID: ${requestId})`);
  
  // You can integrate with a toast library here
  // For now, we'll use console and alert
  const notificationMessage = `${message}\n\nService: ${service}\nRequest ID: ${requestId}`;
  
  // Show in console
  console.error(`[USER_NOTIFICATION] ${notificationMessage}`);
  
  // Also show alert for visibility during testing
  alert(`Error: ${notificationMessage}`);
  
  return notificationMessage;
};