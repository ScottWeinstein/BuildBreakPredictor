function [theta] = trainModel(X, y, lambda)

m = size(X,1);

initial_theta = zeros(size(X, 2), 1);

% Create "short hand" for the cost function to be minimized
costFunction = @(t) lrCostFunction(X, y, t, lambda);

% Now, costFunction is a function that takes in only one argument
options = optimset('MaxIter', 200, 'GradObj', 'on');

% Minimize using fmincg
%theta = fmincg(costFunction, initial_theta, options);

[theta, J, exit_flag] = ...
	fmincg(@(t)(lrCostFunction(X, y, t, lambda)), initial_theta, options);
%fminunc